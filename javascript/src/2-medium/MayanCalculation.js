var inputs = readline().split(' ');
var L = parseInt(inputs[0]);
var H = parseInt(inputs[1]);

var numerals = new Numerals(L, H);

for (var i = 0; i < H; i++) {
    var line = readline();
    numerals.addLine(line);
}

var S1 = parseInt(readline());
var operator1 = new Operator();
var numeral1 = new Numeral(0, L, H);
for (var i = 0; i < S1; i++) {
    var num1Line = readline();
    numeral1.addLine(num1Line);
    if ((i + 1) % H == 0) {
        debug(numeral1);
        operator1.push(numeral1);
        numeral1 = new Numeral(0, L, H);
    }
}
debug(operator1);

var S2 = parseInt(readline());
var operator2 = new Operator();
var numeral2 = new Numeral(0, L, H);
for (var i = 0; i < S2; i++) {
    var num2Line = readline();
    numeral2.addLine(num2Line);
    if ((i + 1) % H == 0) {
        operator2.push(numeral2);
        numeral2 = new Numeral(0, L, H);
    }
}
debug(operator2);

var operationSign = readline();
var operation = new Operation(operator1, operator2, operationSign);
operation.printResultEncoded();

function debug(value) {
    printErr(JSON.stringify(value));
}

function Numerals(large, height) {
    this.numerals = [];
    for (var i = 0; i < 20; i++) {
        this.numerals.push(new Numeral(i, large, height));
    }

    this.addLine = function (line) {
        for (var i = 0; i < 20; i++) {
            this.numerals[i].addLine(line);
        }
    }

    this.valueOf = function (numeralToFind) {
        for (var key in this.numerals) {
            if (this.numerals[key].equals(numeralToFind)) {
                return parseInt(key, 10);
            }
        }
        throw "numeral not found";
    }

    this.get = function (id) {
        return this.numerals[id];
    }
}

function Numeral(id, large, height) {
    this.id = id;
    this.large = large;
    this.height = height;
    this.representation = [];

    this.addLine = function (line) {
        this.representation.push(line.slice(this.id * this.large, (this.id + 1) * this.large));
    }

    this.equals = function (otherNumeral) {
        for (var key in this.representation) {
            if (this.representation[key] != otherNumeral.representation[key]) {
                return false;
            }
        }
        return true;
    }

    this.print = function () {
        for (var key in this.representation) {
            print(this.representation[key]);
        }
    }
}

function Operator() {
    this.numerals = [];

    this.push = function (numeral) {
        this.numerals.splice(0, 0, numeral);
    }

    this.value = function () {
        var result = 0;
        for (var i = 0; i < this.numerals.length; i++) {
            result += numerals.valueOf(this.numerals[i]) * Math.pow(20, i);
        }
        return result;
    }
}

function Operation(op1, op2, sign) {
    this.sign = sign;
    this.op1 = op1;
    this.op2 = op2;
    this.result = 0;

    switch (sign) {
        case '+':
            this.result = this.op1.value() + this.op2.value();
            break;
        case '-':
            this.result = this.op1.value() - this.op2.value();
            break;
        case '*':
            this.result = this.op1.value() * this.op2.value();
            break;
        case '/':
            this.result = this.op1.value() / this.op2.value();
            break;
    }
    ;

    this.getResult = function () {
        return this.result;
    };

    this.printResultEncoded = function () {
        var resultBase20 = this.getResult();
        debug(resultBase20);
        resultBase20 = resultBase20.toString(20).split('');
        resultBase20.forEach(function (numeral) {
            numerals.get(parseInt(numeral, 20)).print();
        })
    }
}