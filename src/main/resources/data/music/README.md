
# Chord Files

Use the following command to import chords and chord formulas into MongoDB

See [mongoimport](https://docs.mongodb.com/manual/reference/program/mongoimport/) for details.

`mongoimport --type json --collection chord_formulas --db music --file "chord_formulas.json"`

# Scale Files
* `mongoimport --type json --db music --collection scale_formulas --file "common_scaleFormulas.json"`
* `mongoimport --type json --db music --collection scale_formulas --file "theoretical_scaleFormulas.json"`
* `mongoimport --type json --db music --collection scales_C --file "scales-C.json"`

# Importing songs
Commands listed below should be executed from the `<root>/src/main/resources/data/music/songs` folder.
* `mongoimport --type json --collection songs --db music --file "Being For The Benefit of Mr. Kite.json"`
* `mongoimport --type json --collection songs --db music --file "Fixing a Hole.json"`
* `mongoimport --type json --collection songs --db music --file "Getting Better.json"`
* `mongoimport --type json --collection songs --db music --file "Good Morning.json"`
* `mongoimport --type json --collection songs --db music --file "Lovely Rita.json"`
* `mongoimport --type json --collection songs --db music --file "Lucy In the Sky With Diamonds.json"`
* `mongoimport --type json --collection songs --db music --file "Penny Lane.json"`
* `mongoimport --type json --collection songs --db music --file "Sgt Pepper's Lonely Hearts Club Band.json"`
* `mongoimport --type json --collection songs --db music --file "She's Leaving Home.json"`
* `mongoimport --type json --collection songs --db music --file "When I'm Sixty-Four.json"`
* `mongoimport --type json --collection songs --db music --file "With A Little Help From My Friends.json"`
* `mongoimport --type json --collection songs --db music --file "Strawberry Fields Forever.json"`

# Other imports (optional)
## Fractals
* `mongoimport --type json --db music --collection "sierpinski" --file "sierpinski.json"`

## Rhythm Scales
* `mongoimport --type json --db music --collection rhythm_scales --file "StandardRhythmScale.json"`
* `mongoimport --type json --db music --collection rhythm_scales --file "Monophonic16StandardRhythmScale.json"`
