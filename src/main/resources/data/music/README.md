
# Chord Files

Use the following command to import chords and chord formulas into MongoDB

`mongoimport --type json --collection chord_formulas --db test --file chord_formulas.json`

# Scale Files
* `mongoimport --type json --collection scale_formulas --db test --file common_scaleFormula.json`
* `mongoimport --type json --collection scale_formulas --db test --file theoretical_scaleFormula.json`

# Importing songs into MongoDB collections
This is optional if you want to use MongoDB for music data. Commands listed below should be
executed from the `<root>/src/resources/data/music/songs` folder.
* `mongoimport --type json --collection songs --db test --file "Being For The Benefit of Mr. Kite.json"`
* `mongoimport --type json --collection songs --db test --file "Fixing a Hole.json"`
* `mongoimport --type json --collection songs --db test --file "Getting Better.json"`
* `mongoimport --type json --collection songs --db test --file "Good Morning.json"`
* `mongoimport --type json --collection songs --db test --file "Lovely Rita.json"`
* `mongoimport --type json --collection songs --db test --file "Lucy In the Sky With Diamonds.json"`
* `mongoimport --type json --collection songs --db test --file "Penny Lane.json"`
* `mongoimport --type json --collection songs --db test --file "Sgt Pepper's Lonely Hearts Club Band.json"`
* `mongoimport --type json --collection songs --db test --file "She's Leaving Home.json"`
* `mongoimport --type json --collection songs --db test --file "When I'm Sixty-Four.json"`
* `mongoimport --type json --collection songs --db test --file "With A Little Help From My Friends.json"`