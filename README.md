
# OpenSlay
OpenSlay is an open source version of Sean O'Connor's 1995 turn based strategy game Slay.

## About
OpenSlay is a territory management style game, where you start with a randomized set of tiles and have to fight your opponents until you are the last standing. Battle both enemies and nature to secure your land and your income.

## Rules

### Territories

Territories are granted a capital when they reach a size of 2 or greater. It is often optimal to merge smaller territories into larger ones, as they become stronger by doing so.

Every hex in your territory generates one gold in income, which is added to the territory's treasury so long as it has a capital. 

If your capital runs out of money to pay the units, all off the units will die and graves will be put in their place.

### Units

Create units by clicking on a territory and clicking on the peasant in the side bar. This will subtract 10 gold from your treasury. Now, place the peasant on the same territory it was purchased from.

To gain access to higher power units, you must combine them by place one atop another. Two peasants make a spearman, and you can add peasants to convert the spearman further into a knight then baron. You can also combine two spearmen to crate a baron, and any other combination that results in a valid unit.

Be warned, as you must pay drastically higher wages for the more powerful units. Power and wages are as follows:

Peasant: 2 / 1
Spearman: 6 / 2
Knight: 18 / 3
Baron: 54 / 4

A unit can move infinitely within its territory, as long as it does not perform an action. Units can also move after merging, as long as they both are still able to be moved.

### Attacking

All units, capitals, and forts create a bubble of defense for the tiles around them. Your units can only move on hexes adjacent to your territory that are undefended, or under-defended. Your unit must have at least one higher strength to overpower a defended hex. Capitals count as 2 strength, while forts count as 3.

### Trees
Trees are an environmental factor of OpenSlay, which prevent the player from earning income from a hex.

Pine trees will grow on a hex adjacent to two other pine trees.

Palm trees (Cacti in OpenSlay) grow on coastal hexes adjacent to another palm tree. 

Move or place a unit on a tree to chop it down. This uses up the unit for the turn.

Tree growth can rapidly get out of hand, so don't shrug them off!

