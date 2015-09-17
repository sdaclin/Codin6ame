var cardRegexp = /(.*)([DHCS])/;
var deckP1 = new Deck();
var deckP2 = new Deck();

var n = parseInt(readline()); // the number of cards for player 1
for (var i = 0; i < n; i++) {
    var card = cardRegexp.exec(readline());
    deckP1.addCards([new Card(card[1],card[2])]);
}
var m = parseInt(readline()); // the number of cards for player 2
for (var i = 0; i < m; i++) {
    var card = cardRegexp.exec(readline());
    deckP2.addCards([new Card(card[1],card[2])]);
}

debug (deckP1);
debug (deckP2);

var wargame = new Wargame(deckP1,deckP2);

wargame.play();

print(wargame.getResult());

function Wargame(deck1,deck2){
    this.stepCount=0;
    this.pat=false;
    this.battleEngaged=false;
    this.battleCardsP1 = [];
    this.battleCardsP2 = [];

    this.play = function(){

        try {
            while (deck1.size() > 0 && deck2.size() > 0) {
                var cardPlayer1 = deck1.dealCard(this.battleEngaged);
                this.battleCardsP1.push(cardPlayer1);
                var cardPlayer2 = deck2.dealCard(this.battleEngaged);
                this.battleCardsP2.push(cardPlayer2);

                var result = cardPlayer1.compareTo(cardPlayer2);
                if (result == 0) {
                    //battle
                    this.battleEngaged = true;
                    this.battleCardsP1.push(deck1.dealCard(this.battleEngaged));
                    this.battleCardsP1.push(deck1.dealCard(this.battleEngaged));
                    this.battleCardsP1.push(deck1.dealCard(this.battleEngaged));
                    this.battleCardsP2.push(deck2.dealCard(this.battleEngaged));
                    this.battleCardsP2.push(deck2.dealCard(this.battleEngaged));
                    this.battleCardsP2.push(deck2.dealCard(this.battleEngaged));
                }else if (result > 0) {
                    this.stepCount++;
                    // player 1 wins
                    deck1.addCards(this.battleCardsP1);
                    deck1.addCards(this.battleCardsP2);
                    this.battleCardsP1 = [];
                    this.battleCardsP2 = [];
                    this.battleEngaged = false;
                } else {
                    this.stepCount++;
                    // player 2 wins
                    deck2.addCards(this.battleCardsP1);
                    deck2.addCards(this.battleCardsP2);
                    this.battleCardsP1 = [];
                    this.battleCardsP2 = [];
                    this.battleEngaged = false;
                }
            }
        }catch(e){
            if(e instanceof BattleDrawException){
                this.pat=true;
            }
        }
    };

    this.getResult = function(){
        if (this.pat){
            return 'PAT';
        }
        return (deck1.size()>0 ? '1':'2') + ' ' + this.stepCount;
    };
}

function BattleDrawException(){

}

function Card(rank,color){
    this.rank = rank;
    this.color = color;
    var value = parseInt(rank,10);
    if (isNaN(value)) {
        switch(rank){
            case 'J':
                value = 11;
                break;
            case 'Q':
                value = 12;
                break;
            case 'K':
                value = 13;
                break;
            case 'A':
                value = 14;
                break;
        }
    }

    this.value = value;

    this.getValue = function() {
        return value;
    };

    this.compareTo = function(otherCard){
        return value - otherCard.getValue();
    };
}

function Deck(){
    this.cards = [];

    this.size = function(){
        return this.cards.length;
    };

    this.addCards = function(cards){
        var that = this;
        cards.forEach(function(cardToAdd){
            that.cards.push(cardToAdd);
        })
    };

    this.dealCard = function(battleEngaged){
        if (battleEngaged != null && battleEngaged && this.cards.length == 0) {
            throw new BattleDrawException();
        }
        return this.cards.shift();
    };
}

function debug(value){
    printErr(JSON.stringify(value));
}