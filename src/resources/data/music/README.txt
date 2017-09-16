Import the following data sets into MongoDB collections:

chord_formulas (37 documents):
grep -v "//" chord_formulas.json>chords.json
mongoimport --type json --collection chord_formulas --db test --file chords.json

scale_formulas (2180 documents):
mongoimport --type json --collection scale_formulas --db test --file common_scaleFormula.json
mongoimport --type json --collection scale_formulas --db test --file theoretical_scaleFormula.json

songs (cd <root>/ressources/data/music/songs   11 documents):
mongoimport --type json --collection songs --db test --file "Being For The Benefit of Mr. Kite.json"
mongoimport --type json --collection songs --db test --file "Fixing a Hole.json"
mongoimport --type json --collection songs --db test --file "Getting Better.json"
mongoimport --type json --collection songs --db test --file "Good Morning.json"
mongoimport --type json --collection songs --db test --file "Lovely Rita.json"
mongoimport --type json --collection songs --db test --file "Lucy In the Sky With Diamonds.json"
mongoimport --type json --collection songs --db test --file "Penny Lane.json"
mongoimport --type json --collection songs --db test --file "Sgt Pepper's Lonely Hearts Club Band.json"
mongoimport --type json --collection songs --db test --file "She's Leaving Home.json"
mongoimport --type json --collection songs --db test --file "When I'm Sixty-Four.json"
mongoimport --type json --collection songs --db test --file "With A Little Help From My Friends.json"




