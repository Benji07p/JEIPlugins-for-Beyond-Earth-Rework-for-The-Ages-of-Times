# Beyond Earth's JEIPlugins reworked for The Ages of Times
Beyond Earth's JEIPlugins reworked for The Ages of Times.
The goal is to separate the fuel loading recipe by Tier Rocket in JEI.
By modifying the corresponding tag, the recipe in JEI changed but **not** his behavior.

List of the tag :
- fuel.json (or FLUID_FUEL_TAG) is the vehicule_fuel display in JEI (vehicule_fuel.json can be modify to change the fuel accepted by **all** of the Rocket and the Rover)
- hydrogen.json (or HYDROGEN_FLUID_TAG) is the list of tags for the new recipe for fuel loading in Tier 1 Rocket (by default, it's water)
- lava.json (or LAVA_FLUID_TAG) is the list of tags for change the recipes for fuel loading in Tier 2 Rocket (by default, it's lava)
- water.json (or WATER_FLUID_TAG) is the list of tags for change the recipes for fuel loading in Tier 3 Rocket (by default, it's water)
- oxygene.json (or OXYGENE_FLUID_TAG) is the list of tags for change the recipes for fuel loading in Tier 4 Rocket (by default it's lava)

To modify this tags, you can create a mod and create this file ressources/data/beyond_earth/tags/fluids/water.json by example and add content to this file.
You can also use kubejs if you're a modpack maker, you can create the file kubejs/data/beyond_earth/tags/fluids/water.json by example and add content to this file.

If you don't want to keep water or lava in JEI, change the line '"replace": false' by '"replace": true' when you modify the JSON files.

Beyond Earth by MrScautHD and his team.

JEI (Just Enough Items) by mezz.
