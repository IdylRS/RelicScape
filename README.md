# RelicScape

This plugins goal is to create Leagues-style restrictions and game play to hold us over until Jagex finally makes Leagues 4 a reality. The core gameplay comes down to unlockings regions and skills by using points. Points are gained through finding relics across Gielnor by trianing your skills, killing monsters, completing clue scrolls, and, of course, completing tasks.

**This plugin is still in beta, so bugs will be present and many changes will be made**

## Support

If you'd like to support me and the development of this plugin. consider subscribing to my 
[YouTube channel](https://www.youtube.com/idylrs). It's free!

## Thank you

I want to thank the developers of the plugins that play a part (big or small) in this plugin. Code from these plugins
was used and modified to create one cohesive RelicScape plugin.

### slaytostay  - [Region Locker](https://github.com/slaytostay/region-locker)

The region locker and region GPU plugins are key parts of RelicScape that have been modified to work
with the trailblazer region system. This plugin would not exist without his work & the collaborators on that project.

### Antipixel - [Nexus Map](https://github.com/Antipixel/nexus-map)

A modified version of the nexus map plugin serves as the basis for the region unlock
UI. I learned a lot from reading and modifying Antipixel's code to make it work this use case.

### osrs-reldo - [Task Tracker](https://github.com/osrs-reldo/tasks-tracker-plugin)

The task tracker plugin served as the inspiration for the task panel in RelicScape. I had no idea how to create panels,
but reading through Reldo's code made it a much smoother experience. Their panel is still much nicer, though.

### CodePanter - [another-bronzeman-mode](https://github.com/CodePanter/another-bronzeman-mode)

Reading through this code taught me a lot about creating a basically new game inside of a runelite plugin. Saving and loading player data
would not be in RelicScape without the help of their code base.

# How to play

RelicScape is designed to be played on new accounts, but you can play on any account you want. Level and quest
tasks will be autocompleted when you log in (this can take a few seconds, don't panic).

## Starting off
You being with 1500 points, no regions, and no skills. Use the points wisely as you cannot refund an unlock.

## Unlocking regions
To unlock a region, open the world map and click the checkbox in the top left. 
From the region unlock UI, you can click on the region icons to go to a confirmation screen
where you may unlock or re-lock regions.

![Region unlock screen](https://i.imgur.com/c1eed3U.png)

Players may unlock a maximum of 3 regions, but this can be changed in the settings.

## Unlocking skills
Skills can be unlocked or re-locked by right-clicking them in the skill interface.

![unlocking skills](https://i.imgur.com/sWAvMER.png)

If you gain xp in a skill you have not unlocked, a warning message will appear in your chat box.

## Obtaining Relics

Relics of varying tiers can be obtained a multitude of ways. First, here is a breakdown of relics and their point values:

* Tier 1: 10 points
* Tier 2: 25 points
* Tier 3: 50 points
* Tier 4: 100 points
* Tier 5: 250 points
* Tier 6: 500 points

### Killing monsters

Monsters have a chance of dropping a relic based on their combat level.
The higher the combat level, the higher the chance. Higher combat level monsters
also have a chance of dropping higher tier relics (up to tier 3).

* Level 1-50: Maximum Tier 1
* Level 50-200: Maximum Tier 2
* Level 200+:  Maximum Tier 3

### Training skills

Any non-combat xp gained gives you a chance at a tier 1 relic. The more xp gained, the higher the chance (up to a limit).

### Completing tasks

There are hundreds of tasks to be completed. These tasks range from tier 1 to tier 2, corresponding to the points you
receive upon completing them. They can be viewed in the RelicScape side panel.

![side panel](https://i.imgur.com/7Q08fnz.png)

### Completing clues

When you open a casket, you have a chance of receiving a relic. The chance of a relic and the tier of the relic
are based on the difficulty of the clue.

* Beginner: 50% chance of relic, Tier 1 relic
* Easy: 50% chance of relic, likely Tier 1 relic, chance for Tier 2
* Medium: 50% chance of relic, likely Tier 1 or 2 relic, small chance for Tier 3
* Hard: 60% chance of relic, likely Tier 2 relic, small chance for Tier 3
* Elite: 70% chance of relic, likely Tier 2 or 3 relic, small chance for Tier 4
* Master: 70% chance of relic, likely Tier 2 or 3, chance for Tier 4

# Questions, Comments, Concerns

If you experience a bug, have a suggestion, or just want to say hi, please 
submit an issue in this repository and I'll respond to it as soon as I can.

Follow me on twitter for updates on this and my other plugins: [https://twitter.com/Idyl_rs]()