## Backlog:
- separate from skds core (if possible)
- forge fluid handling compat check: 1) does it work on other fluids than water 2) does it compat with fluid mods (e.g. pipez, mekanism)
- mass conservation check (I am not sure if it is guaranteed, and it would be annoying if redstone machines can glitch away or create fluid from nothing)

## Upgrade & Port
- upgrade 1.17.1 to 1.18
- backport to 1.16.5: adaptive player movement speed in water 

## ideas:
- fluid movement creates current, which pushes/pulls players and items => working?
- water (only) replenishing mechanics (rain, groundwater at sea level) => compensate finite water and ugliness of oceans/rivers after pumping (limited equalization distance creates ditch

