# SFWRENG-2AA4-Catan
A Java-based simulator for the board game Catan, developed for McMaster University's SFWRENG 2AA4 (Software Design I). The simulator supports full game rules, a human-playable Player 1, AI opponents, undo/redo, rule-based machine intelligence, and a live board visualizer.

## Authors
- Komal Khan
- Alisha Faridi
- Maria Shashati
- Rameen Tariq

## Link to SonarQube Badge
[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=komalkhan-0811_SFWRENG-2AA4-Catan)


## How to Run

### 1. Build and run tests
```bash
mvn test
```

### 2. Run the game (Demonstrator)
```bash
mvn compile
mvn exec:java -Dexec.mainClass="catan.Demonstrator"
```
Or run `Demonstrator.java` directly from your IDE.

### 3. Run the live visualizer (optional, in a separate terminal)
```bash
cd /path/to/SFWRENG-2AA4-Catan
source src/visualize/.venv/bin/activate
python src/visualize/light_visualizer.py src/visualize/base_map.json --watch
```

## Setting Up the Python Visualizer (first time only)
```bash
python3 -m venv src/visualize/.venv
source src/visualize/.venv/bin/activate
pip install -r src/visualize/requirements.txt
pip install src/visualize/catanatron
pip install numpy
```

## How to Play

On your turn:
1. Type `roll` to roll the dice
2. Optionally build something:
   - `build settlement <intersectionId>`
   - `build city <intersectionId>`
   - `build road <intersectionA> <intersectionB>`
   - `list` shows all legal actions
3. Type `go` to end your turn

After each AI turn, type `go` to advance to the next turn.

**Building costs:**
| Building   | Cost                              |
|------------|-----------------------------------|
| Road       | 1 Wood, 1 Brick                   |
| Settlement | 1 Wood, 1 Brick, 1 Wheat, 1 Sheep |
| City       | 2 Wheat, 3 Ore                    |

## Running Tests
```bash
mvn test
```
