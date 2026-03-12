import json
from typing import Dict, Optional, Tuple, List
import numpy as np

from catanatron.models.map import (
    CatanMap,
    LandTile,
    initialize_tiles,
    MapTemplate
)
from catanatron.models.board import Board
from catanatron.models.player import Color, Player
from catanatron.models.enums import WOOD, BRICK, SHEEP, WHEAT, ORE, SETTLEMENT, CITY
from catanatron.game import Game
from catanatron.state import State
from catanatron.gym.envs.pygame_renderer import PygameRenderer
from PIL import Image
import sys
import os
import time


class CatanBoardVisualizer:
    """
    Facade for visualizing the Catan board from JSON files.
    This class provides an interface to convert JSON catan board data into
    Catanatron objects and render them using pygame.
    """

    def __init__(self):
        self.map_data: Optional[Dict] = None
        self.state_data: Optional[Dict] = None
        self.game: Optional[Game] = None

    def load_map_json(self, json_path: str) -> None:
        """
        Load base map from JSON file.
        """
        with open(json_path, 'r') as f:
            self.map_data = json.load(f)

    def load_state_json(self, json_path: str) -> None:
        """
        Load game state from JSON file.
        """
        with open(json_path, 'r') as f:
            self.state_data = json.load(f)

    def _parse_resource(self, resource_str: Optional[str]) -> Optional[str]:
        """Convert resource string to FastResource or None for desert."""
        if resource_str is None or resource_str == "DESERT":
            return None

        resource_map = {
            "WOOD": WOOD,
            "BRICK": BRICK,
            "SHEEP": SHEEP,
            "WHEAT": WHEAT,
            "ORE": ORE,
        }

        if resource_str not in resource_map:
            raise ValueError(f"Unknown resource: {resource_str}")

        return resource_map[resource_str]

    def _parse_color(self, color_str: str) -> Color:
        """Convert color string to Color enum."""
        color_map = {
            "RED": Color.RED,
            "BLUE": Color.BLUE,
            "ORANGE": Color.ORANGE,
            "WHITE": Color.WHITE,
        }

        if color_str not in color_map:
            raise ValueError(f"Unknown color: {color_str}")

        return color_map[color_str]

    def _create_map_from_json(self) -> CatanMap:
        """
        Create a CatanMap from the loaded JSON data.
        """
        if self.map_data is None:
            raise ValueError("No map data loaded. Call load_map_json first.")

        # Convert tiles to coordinate: (resource, number) mapping
        tile_coords = []
        resources = []
        numbers = []

        for tile_data in self.map_data["tiles"]:
            coord = (tile_data["q"], tile_data["s"], tile_data["r"])

            if sum(coord) != 0:
                raise ValueError(f"Invalid cube coordinate: {coord}. Sum must be 0.")

            tile_coords.append(coord)
            resource = self._parse_resource(tile_data["resource"])
            resources.append(resource)

            # Only add number if not desert
            if resource is not None:
                numbers.append(tile_data["number"])

        # Build topology from coordinates (now: all land tiles)
        topology = {coord: LandTile for coord in tile_coords}

        # Create MapTemplate
        template = MapTemplate(
            numbers=numbers,
            port_resources=[],
            tile_resources=resources,
            topology=topology,
        )

        numbers_reversed = list(reversed(numbers))
        resources_reversed = list(reversed(resources))

        # Initialize tiles using the template
        tiles = initialize_tiles(
            template,
            shuffled_numbers_param=numbers_reversed,
            shuffled_port_resources_param=[],
            shuffled_tile_resources_param=resources_reversed,
        )

        # Create and return the map
        catan_map = CatanMap.from_tiles(tiles)

        return catan_map

    def _apply_state_to_board(self, board: Board):
        from catanatron.models.enums import SETTLEMENT, CITY

        # Clear catanatron's own initial placement
        board.buildings = {}
        board.roads = {}

        # Write buildings directly to dict
        for building_data in self.state_data.get("buildings", []):
            node_id = building_data["node"]
            color = self._parse_color(building_data["owner"])
            building_type = SETTLEMENT if building_data["type"] == "SETTLEMENT" else CITY
            board.buildings[node_id] = (color, building_type)

        # Write roads directly to dict
        for road_data in self.state_data.get("roads", []):
            a = road_data["a"]
            b = road_data["b"]
            color = self._parse_color(road_data["owner"])
            board.roads[(a, b)] = color
            board.roads[(b, a)] = color

    def build_game(self) -> Game:
        """
        Build a Game object from the loaded JSON data.

        Returns:
            Game object ready for rendering
        """
        # Create map
        catan_map = self._create_map_from_json()

        # Create dummy players (needed for Game initialization)
        # We don't need actual players for visualization
        players = [
            Player(Color.BLUE),
            Player(Color.RED)
        ]

        # Create game with the map
        game = Game(
            players=players,
            seed=42,
            catan_map=catan_map,
            initialize=True,
        )

        # Apply state to the board
        self._apply_state_to_board(game.state.board)

        self.game = game
        return game

    def render(
            self,
            output_dir: Optional[str] = None,
            render_scale: float = 1.0,
            show: bool = False,
    ) -> np.ndarray:
        """
        Render the board and save to file.
        Args:
            output_dir: Directory to save the rendered image (PNG). If None, doesn't save.
            render_scale: Scale factor for rendering (default 1.0)
            show: Whether to display the image (requires display)

        Returns:
            RGB numpy array of the rendered board
        """
        if self.game is None:
            self.build_game()

        # Create renderer
        renderer = PygameRenderer(render_scale=render_scale)

        # Render the game
        rgb_array = renderer.render(self.game)

        # Save image
        if output_dir is None:
            output_dir = "scraped_boards"
        os.makedirs(output_dir, exist_ok=True)
        file_count = len(
            [f for f in os.listdir(output_dir) if os.path.isfile(os.path.join(output_dir, f))]
        )
        output_path = os.path.join(output_dir, f"board{file_count}.png")

        img = Image.fromarray(rgb_array)
        img.save(output_path)
        print(f"Board rendered and saved to {output_path}")

        # Clean up
        renderer.close()

        return rgb_array



def visualize_board_from_json(
        map_json_path: str,
        state_json_path: str,
        output_dir: str = "scraped_boards",
        render_scale: float = 1.0,
) -> None:
    """
    Convenience function to visualize a Catan board from JSON files.

    Args:
        map_json_path: Path to map JSON file
        state_json_path: Path to state JSON file
        output_dir: Path to save rendered image
        render_scale: Rendering scale factor (higher = bigger image)
    """
    visualizer = CatanBoardVisualizer()
    visualizer.load_map_json(map_json_path)
    visualizer.load_state_json(state_json_path)
    visualizer.render(output_dir=output_dir, render_scale=render_scale)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python light_visualizer.py base_map.json state.json")
        print("  python light_visualizer.py base_map.json --watch")
        sys.exit(1)
    base_map_path = sys.argv[1]
    watch_mode = "--watch" in sys.argv
    state_path = os.path.join(os.path.dirname(base_map_path), "state.json")
    if len(sys.argv) >= 3:
        if sys.argv[2] != "--watch" and sys.argv[2].endswith(".json"):
            state_path = sys.argv[2]
    last_mtime = None
    last_fsize = None
    print("Visualizer started.")
    if watch_mode:
        print("Watch mode enabled. Waiting for state.json changes...")
    while True:
        if os.path.exists(state_path):
            mtime = os.path.getmtime(state_path)
            fsize = os.path.getsize(state_path)
            if (not watch_mode) or (mtime != last_mtime) or (fsize != last_fsize):
                last_mtime = mtime
                last_fsize = fsize
                visualize_board_from_json(base_map_path,state_path)
        if not watch_mode:
            break
        time.sleep(0.5)