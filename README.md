# Rot

Rot is a desktop creature-catching RPG built with Java Swing. Explore a tile-based
world, collect and train BrainRots, challenge trainers and gyms, complete quests,
earn badges, and build a six-member party backed by PC storage.

## Highlights

- Turn-based battles with type matchups, status effects, stat changes, items,
  capture mechanics, move UP, switching, XP, and level-up learnsets
- Tactical opponent AI that evaluates damage, knockouts, healing, status moves,
  buffs, debuffs, matchup advantage, and remaining UP
- Live battle readout showing damage range, effectiveness, and turn order before
  committing to a move
- Multiple maps with encounters, trainer sight lines, loot, shops, teleporters,
  cave lighting, and persistent defeated-trainer state
- Quest, badge, inventory, save-slot, map, PC storage, and narrative systems

## Controls

| Input | Action |
| --- | --- |
| `WASD` / Arrow keys | Move and navigate menus |
| `Shift` | Sprint |
| `E` | Interact |
| `Enter` | Confirm |
| `Esc` | Back / pause menu |
| `B` | Quick party access |
| `M` | Map |
| `Tab` | Contextual interface action |

## Running the game

The project requires a JDK with modern switch-expression support (Java 17 or
newer is recommended) and the bundled `libs/jl1.0.1.jar` audio dependency.

In IntelliJ IDEA, open the `Rot` directory, set the project SDK, and run
`src/main/Main.java`.

From a Unix-like terminal:

```sh
mkdir -p out
javac -cp libs/jl1.0.1.jar -d out $(find src -name '*.java')
java -cp "out:libs/jl1.0.1.jar" main.Main
```

On Windows, replace the runtime classpath separator (`:`) with `;`.

## Project layout

```text
src/battle       Combat rules, rewards, capture, status, turn order, and AI
src/brainrots    Creature model, registry, tiers, stats, and learnsets
src/engine       Main panel and game loop
src/overworld    Player movement, encounters, and shops
src/progression  Quests, narrative, gyms, and badges
src/ui           Swing-rendered game interfaces and effects
src/res          Maps, sprites, audio, fonts, saves, and other assets
```
