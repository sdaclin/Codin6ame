var dictionnary = new Dictionnary();

var N = parseInt(readline());
for (var i = 0; i < N; i++) {
    var W = readline();
    dictionnary.add(W);
}
debug(dictionnary);

var LETTERS = readline();
debug(LETTERS);

dictionnary.removeWordWithMissingLetters(LETTERS);
debug(dictionnary);

print(dictionnary.findBetterWord());

// Write an action using print()
// To debug: printErr('Debug messages...');

function Dictionnary() {
    this.words = [];
    this.lettersValue = {
        e: 1, a: 1, i: 1, o: 1, n: 1, r: 1, t: 1, l: 1, s: 1, u: 1,
        d: 2, g: 2,
        b: 3, c: 3, m: 3, p: 3,
        f: 4, h: 4, v: 4, w: 4, y: 4,
        k: 5,
        j: 8, x: 8,
        q: 10, z: 10
    };

    this.add = function (word) {
        this.words.push(word);
    }

    this.removeWordWithMissingLetters = function (availableLetters) {
        var position;
        var currentLetter;
        this.words = this.words.reduce(function (newWords, currentWord) {
            var letters = availableLetters.split('');
            var currentWordArr = currentWord.split('');
            //debug(currentWord);
            while ((currentLetter = currentWordArr.pop()) != null) {
                //debug("           " + currentLetter);
                if ((position = letters.indexOf(currentLetter)) == -1) {
                    // Le mot a une lettre qui n'est pas // plus dans les lettres du scrabble
                    //debug("Lettre pas dans le dico ["+currentLetter+"] zapping word ["+currentWord+"]");
                    return newWords;
                }
                letters.splice(position, 1);
            }
            if (currentWordArr.length > 0) {
                // Le mot a plus de lettre que les lettres du scrabble
                //debug("Mot a trop de lettre ["+currentWord+"]")
                return newWords;
            }
            //debug("Mot est valide ["+currentWord+"]")
            newWords.push(currentWord);
            return newWords;
        }, []);
    }

    this.findBetterWord = function () {
        var betterScore = 0;
        var betterWord;
        var that = this;
        this.words.forEach(function (currentWord) {
            var letters = currentWord.split('');
            var currentScore = letters.reduce(function (score, currentLetter) {
                return score += that.lettersValue[currentLetter];
            }, 0);
            if (currentScore > betterScore) {
                betterScore = currentScore;
                betterWord = currentWord;
            }
        });
        return betterWord;

    }
}

function debug(value) {
    printErr(JSON.stringify(value));
}