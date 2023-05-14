## confirmed bugs
- frost walker dupe:
  - frost walker transforms full(!) water blocks (at the same level as the walker) into ice (in radius)
  - the created ice pushes the water away!!!!
  - After ice melts it creates another block of water
- not-full blocks get **destroyed** by water when water placed (bucket) on adjacent block through empty parts of block model
  - affected blocks
    - hopper, lectern, extended piston, (redstone) torch, comparator, pressure plate, redstone dust, snow, sapling,
      fungus, flowers, grass, cauldron, brewing stand, farmland, ...
  - not affected non-full blocks
    - get waterlogged (any water level): doors, trapdoor, ladder, fence, sign, sea pickle, sign, stairs, slabs, chest, ...
    - get waterlogged (only full block/level=8): lightning rod (level 8), all rails, wall
      - check if rails accept lower partials or just flow over (lower collision box)
      - if receives partial water block => will try to expel; if not possible will **create** water to fill to 8
        - e.g. 
        - when partial bucket directly into block: water is **created** to fill up to 8 (and then it tries to flow away)
        - when partial water falls on block and cannot escape (chimney), stays on top and does not waterlog
        - when full water falls on block (or partial becomes full) drops down and fills the block
        - when _lateral_ fluid EQ sends partial water into block and cannot escape, water is **created** to fill block
          - pit with 1 water block next to this block => add 1 level to water block => fills this block (water created)
        - _diagonal_/_vertical_ fluid EQ sends bottom water to side before lowering top water
          - pit with 1 water block next to this block => add 1 level to block above water => this filled, next to it level 1 (no water created)
        - partial water flows through block (blocks left and right equalize normally)
        - fluid EQ is faster than this block emptying from full water
        - when this block filled (minecraft:water) next to partial water (minecraft:flowing_water) => creates current from block to partial
## bugs from wpo github:
### internal/vanilla bugs
- snowy grass bug: when overflowing water destroys snow, the grass block stays snowy (texture)
- water-lava interaction creates lags
- water can be placed in nether
- ocean vegetation (waterloggable blocks) does not spawn waterlogged => fix on worldgen?
- sponge problems?
### mod interaction bugs
- set waterlogged crash [mekanism, immersive engineering, ...]: not working with new blockstate json parsing system (1.16.2+)
  - possible solution: use Statement mod to add waterlogged property to all blocks - https://github.com/Sasai-Kudasai-BM/Water-Physics-Overhaul/issues/6
    - https://www.curseforge.com/minecraft/mc-mods/statement (forge and fabric)
  - crash logs:
    - https://github.com/Sasai-Kudasai-BM/Water-Physics-Overhaul/issues/53
    - https://github.com/Sasai-Kudasai-BM/Water-Physics-Overhaul/issues/14
    - https://github.com/Sasai-Kudasai-BM/Water-Physics-Overhaul/issues/6
- mekanism:
    - water destroys mekanism blocks
  - set waterlogged crashing (Mekanism Additions, Mekanism Generators, Mekanism Tools) https://github.com/Sasai-Kudasai-BM/Water-Physics-Overhaul/issues/17
- immersive-engineering: 
    - crash when placing on waterwheel/IE blocks (set waterlogged crash)
- twilight forest: no trees generating?
- nuclear-science compat: https://github.com/Sasai-Kudasai-BM/Water-Physics-Overhaul/issues/26
- dynamic trees: get waterlogged and dupe water
- create mod: pump and pipeline compat

## important:
- config for fluid update/tick speed (reduce strain on weak machines)
- forge fluid compat check: 
    - does it work on other fluids than water (flowing/pushing, rendering, adaptive walking speed)
    - does it compat with fluid transportation mods (e.g. pipez, mekanism)
- mass conservation check (I am not sure whether it is guaranteed: fluid should not be glitch created/destroyed)

## good to have:
- tests (for all added features?)
- fluid movement creates current, which pushes/pulls players and items
- separate from skds core (if possible)

## ideas:
- water (only) replenishing mechanics (rain, wells/groundwater at sea level) => compensate finite water and ugliness of oceans/rivers after pumping (limited equalization distance creates ditch
  - check wpo addon (source code?)
- waterfalls: 
  - water source and void for rivers etc? reasonably fast?
  - temporary water?
- make only server-side version? (remove rendering etc...). maybe configurable only?

## Upgrade & Port
- backport to 1.12
- port features/fixes to 1.16.5: 
  - adaptive player movement speed in water
- port features/fixes to 1.17.1:
  - ...
- port features/fixes to 1.18.2:
  - ...

## DONE
* upgrade 1.17.1 to 1.18