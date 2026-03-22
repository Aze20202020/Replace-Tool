# Replace Tool Mod — Minecraft Forge 1.19.2

## Overview
A creative-mode building utility that lets you select a 3D region and replace all blocks inside it with any block you choose (including blocks from other mods).

## How to Use

1. **Hold** the Replace Tool (found in the Tools creative tab).
2. **Right-click** any block → sets **Position 1** (first corner of your selection).
3. **Right-click** another block → sets **Position 2** (opposite corner). All blocks between these two points are included (like WorldEdit's cuboid selection).
4. **Right-click** any block once more → opens the **Replace GUI**.
5. In the GUI:
   - Type the block ID to replace with (e.g. `minecraft:stone`, `minecraft:oak_planks`, `create:andesite_alloy`).
   - Click **Replace with Block** to replace all selected blocks with your chosen block.
   - Click **Replace with Air** to delete all selected blocks.
   - Click **Cancel** to abort.

## Features
- ✅ Unlimited selection size (no volume cap)
- ✅ Works with blocks from any mod — just type the mod's block ID
- ✅ Automatically force-loads all chunks in the selection zone before replacing
- ✅ Processes large selections in batches (65,536 blocks per batch) to avoid server freezes
- ✅ Progress messages in chat for large operations
- ✅ Creative mode only — won't work in survival/adventure
- ✅ Tooltip shows current selection state

## Block ID Examples
| Block | ID |
|---|---|
| Stone | `minecraft:stone` |
| Oak Planks | `minecraft:oak_planks` |
| Air (delete) | Use "Replace with Air" button |
| Create Andesite Casing | `create:andesite_casing` |
| Botania Livingwood | `botania:livingwood` |

## Technical Notes
- Chunks are force-loaded before the operation and un-forced after completion.
- Block placement uses `Block.UPDATE_ALL` so lighting, neighbors, and tile entities all update correctly.
- The server does all the work — this is fully server-side safe and works in multiplayer (operator/creative required).
