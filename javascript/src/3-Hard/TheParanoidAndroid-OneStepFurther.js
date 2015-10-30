const SIMU = 0;
const WEB = 1;
const TEST = 2;

const ACTION_WAIT = 'WAIT';
const ACTION_ELEVATOR = 'ELEVATOR';
const ACTION_BUILTIN_ELEVATOR = 'BUILTIN_ELEVATOR';
const ACTION_BLOCK = 'BLOCK';

const DIRECTION_LEFT = 'LEFT';
const DIRECTION_RIGHT = 'RIGHT';

const MAP_ELEVATOR = 'E';
const MAP_EXIT = 'G';
const MAP_START = 'S';

// #2 |.O........|  Out
// #1 |.....E....|  Elevator
// #0 |....S.....|  Start
const lvlTest = '{"nbFloors":3,"width":10,"nbRounds":6,"exitFloor":2,"exitPos":2,"nbTotalClones":4,"nbAdditionalElevators":1,"nbElevators":1,"elevators":[{"elevatorFloor":2,"elevatorPos":6}],"marvinStartingState":{"floor":0,"pos":5,"direction":"RIGHT"}}';

const lvl1 = '{"nbFloors":2,"width":13,"nbRounds":100,"exitFloor":1,"exitPos":11,"nbTotalClones":10,"nbAdditionalElevators":1,"nbElevators":0,"elevators":[],"marvinStartingState":{"floor":0,"pos":2,"direction":"RIGHT"}}';
const lvl3 = '{"nbFloors":6,"width":13,"nbRounds":100,"exitFloor":5,"exitPos":10,"nbTotalClones":10,"nbAdditionalElevators":5,"nbElevators":0,"elevators":[],"marvinStartingState":{"floor":0,"pos":1,"direction":"RIGHT"}}';
const lvl5 = '{"nbFloors":7,"width":13,"nbRounds":30,"exitFloor":6,"exitPos":7,"nbTotalClones":10,"nbAdditionalElevators":3,"nbElevators":3,"elevators":[{"elevatorFloor":2,"elevatorPos":6},{"elevatorFloor":0,"elevatorPos":6},{"elevatorFloor":3,"elevatorPos":7}],"marvinStartingState":{"floor":0,"pos":4,"direction":"RIGHT"}}';
const lvl6 = '{"nbFloors":10,"width":19,"nbRounds":47,"exitFloor":9,"exitPos":9,"nbTotalClones":41,"nbAdditionalElevators":0,"nbElevators":17,"elevators":[{"elevatorFloor":0,"elevatorPos":9},{"elevatorFloor":5,"elevatorPos":4},{"elevatorFloor":2,"elevatorPos":9},{"elevatorFloor":6,"elevatorPos":9},{"elevatorFloor":0,"elevatorPos":3},{"elevatorFloor":7,"elevatorPos":4},{"elevatorFloor":5,"elevatorPos":17},{"elevatorFloor":3,"elevatorPos":17},{"elevatorFloor":2,"elevatorPos":3},{"elevatorFloor":4,"elevatorPos":9},{"elevatorFloor":8,"elevatorPos":9},{"elevatorFloor":7,"elevatorPos":17},{"elevatorFloor":4,"elevatorPos":3},{"elevatorFloor":1,"elevatorPos":17},{"elevatorFloor":1,"elevatorPos":4},{"elevatorFloor":3,"elevatorPos":4},{"elevatorFloor":6,"elevatorPos":3}],"marvinStartingState":{"floor":0,"pos":6,"direction":"RIGHT"}}';
const lvl8 = '{"nbFloors":13,"width":36,"nbRounds":67,"exitFloor":11,"exitPos":12,"nbTotalClones":41,"nbAdditionalElevators":4,"nbElevators":34,"elevators":[{"elevatorFloor":2,"elevatorPos":34},{"elevatorFloor":5,"elevatorPos":34},{"elevatorFloor":4,"elevatorPos":9},{"elevatorFloor":8,"elevatorPos":23},{"elevatorFloor":0,"elevatorPos":34},{"elevatorFloor":4,"elevatorPos":23},{"elevatorFloor":8,"elevatorPos":1},{"elevatorFloor":10,"elevatorPos":3},{"elevatorFloor":6,"elevatorPos":34},{"elevatorFloor":3,"elevatorPos":17},{"elevatorFloor":4,"elevatorPos":34},{"elevatorFloor":5,"elevatorPos":4},{"elevatorFloor":11,"elevatorPos":13},{"elevatorFloor":7,"elevatorPos":34},{"elevatorFloor":9,"elevatorPos":34},{"elevatorFloor":11,"elevatorPos":11},{"elevatorFloor":1,"elevatorPos":34},{"elevatorFloor":7,"elevatorPos":17},{"elevatorFloor":6,"elevatorPos":13},{"elevatorFloor":1,"elevatorPos":4},{"elevatorFloor":2,"elevatorPos":24},{"elevatorFloor":8,"elevatorPos":9},{"elevatorFloor":1,"elevatorPos":17},{"elevatorFloor":11,"elevatorPos":4},{"elevatorFloor":6,"elevatorPos":22},{"elevatorFloor":1,"elevatorPos":24},{"elevatorFloor":10,"elevatorPos":23},{"elevatorFloor":3,"elevatorPos":34},{"elevatorFloor":9,"elevatorPos":17},{"elevatorFloor":2,"elevatorPos":3},{"elevatorFloor":8,"elevatorPos":34},{"elevatorFloor":2,"elevatorPos":23},{"elevatorFloor":10,"elevatorPos":34},{"elevatorFloor":9,"elevatorPos":2}],"marvinStartingState":{"floor":0,"pos":6,"direction":"RIGHT"}}';

var configuration = {};
if (getEnv() == WEB) {
// 3 10 120 3 4
    var inputs;
    inputs = readline().split(' ');
    configuration.nbFloors = parseInt(inputs[0]); // number of floors
    configuration.width = parseInt(inputs[1]); // width of the area
    configuration.nbRounds = parseInt(inputs[2]); // maximum number of rounds
    configuration.exitFloor = parseInt(inputs[3]); // floor on which the exit is found
    configuration.exitPos = parseInt(inputs[4]); // position of the exit on its floor
    configuration.nbTotalClones = parseInt(inputs[5]); // number of generated clones
    configuration.nbAdditionalElevators = parseInt(inputs[6]); // number of additional elevator that you can build

    configuration.nbElevators = parseInt(inputs[7]); // number of elevator
    configuration.elevators = [];
    for (var i = 0; i < configuration.nbElevators; i++) {
        inputs = readline().split(' ');
        var elevator = {};
        elevator.elevatorFloor = parseInt(inputs[0]); // floor on which this elevator is found
        elevator.elevatorPos = parseInt(inputs[1]); // position of the elevator on its floor
        configuration.elevators.push(elevator);
    }

    inputs = readline().split(' ');
    var cloneFloor = parseInt(inputs[0]); // floor of the leading clone
    var clonePos = parseInt(inputs[1]); // position of the leading clone on its floor
    var direction = inputs[2]; // direction of the leading clone: LEFT or RIGHT

    configuration.marvinStartingState = {
        floor: cloneFloor,
        pos: clonePos,
        direction: direction
    };
    debug(configuration);
} else if (getEnv() == TEST) {
    testAll();
    process.exit();
} else {
    configuration = JSON.parse(lvl6);
}

var marvin = new Marvin(configuration);
var stackToGo = marvin.computeSolution();
marvin.printAllActions(stackToGo);

function Marvin(configuration) {
    var that = this;
    this.startingState = new State(configuration.marvinStartingState.pos, configuration.marvinStartingState.floor, configuration.marvinStartingState.direction, configuration.nbAdditionalElevators);
    this.alreadyComputedContext = [];

    // Init LevelMap
    this.levelMap = new LevelMap(configuration.width, configuration.nbFloors);
    configuration.elevators.forEach(function (elevator) {
        that.levelMap.setContent(elevator.elevatorPos, elevator.elevatorFloor, MAP_ELEVATOR);

    });
    this.levelMap.setContent(configuration.exitPos, configuration.exitFloor, MAP_EXIT);
    this.levelMap.setContent(this.startingState.getPos(), this.startingState.getFloor(), MAP_START);
    this.levelMap.print();

    this.verifyStack = function (stack, startingState, finalState, counter) {
        var that = this;
        var currentState = startingState;
        stack.forEach(function (currentAction,currentIndex) {
            currentState = that.do(currentState, currentAction.action, currentIndex>0 ? stack[currentIndex].action:null);
        });
        if (currentState.getPos() != finalState.getPos() || currentState.getFloor() != finalState.getFloor() || currentState.getDirection() != finalState.getDirection()) {
            debug('Something goes wrong at ' + counter);
        }
    };

    this.computeSolution = function () {
        var stack = []; // stackToGoToExit
        this.alreadyComputedContext = [];
        var currentContext = new Context(this.startingState, null);
        var wayToExitIsFound = false;
        var counter = 0;
        while (!wayToExitIsFound) {
            var that = this;


            //if (counter == 67) {
            //    debug('here');
            //}

            //this.verifyStack(stack, this.startingState, currentContext.state, counter++);

            var currentContent = this.levelMap.getContent(currentContext.state.getPos(), currentContext.state.getFloor());
            if (currentContent == MAP_EXIT) {
                wayToExitIsFound = true;
            } else if (currentContent == MAP_ELEVATOR) {
                try {
                    currentContext = this.tryToDo(stack, currentContext, ACTION_BUILTIN_ELEVATOR);
                } catch (err) {
                    // Happens when arriving on an already visited context
                    do {
                        currentContext = stack.pop();
                    } while (currentContext.action == ACTION_BUILTIN_ELEVATOR);
                }
            } else {
                var nextAction;
                if ((nextAction = this.nextAction(currentContext)) != null) {
                    try {
                        // todo Optimisation pour v�rifier si on a d�j� pris ce chemin
                        currentContext = that.tryToDo(stack, currentContext, nextAction);
                    } catch (err) {
                        // If action can't be done, we note it in current context to be set as tried
                        currentContext.setAction(nextAction);
                    }
                } else {
                    // no more action to try
                    do {
                        currentContext = stack.pop();
                        //debug(currentContext);
                    } while (currentContext.action == ACTION_BUILTIN_ELEVATOR);
                }
            }
        }

        debug(stack);
        return stack;
    };

    this.getMoveCostForAction = function(action){
        switch (action) {
            case ACTION_BUILTIN_ELEVATOR:
            case ACTION_WAIT:
                return 1;
                break;
            case ACTION_BLOCK:
                return 3;
                break;
            case ACTION_ELEVATOR:
                return 4;
                break;
        }
    }

    /**
     * Stack action sometime count as many actions
     * @param stack
     * @param lastAction that should become part of the stack
     */
    this.computeStackLength = function (stack, lastAction) {
        var that = this;
        var stackLength = stack.reduce(function (result, context) {
            return result + that.getMoveCostForAction(context.action);
        }, 0);
        return stackLength + this.getMoveCostForAction(lastAction);
    };

    /**
     * Maintain an index of [state.getPos()][state.getFloor()][state.getDirection()][nbElevator] => pathLength
     * @param state
     * @param pathLength
     * @returns {boolean} false if for a given state we have already seen an <= pathLength
     */
    this.indexAndCheckState = function (state, pathLength) {
        if (this.alreadyComputedContext[state.getPos()] == null) {
            this.alreadyComputedContext[state.getPos()] = [];
        }
        if (this.alreadyComputedContext[state.getPos()][state.getFloor()] == null) {
            this.alreadyComputedContext[state.getPos()][state.getFloor()] = [];
        }
        if (this.alreadyComputedContext[state.getPos()][state.getFloor()][state.getDirection()] == null) {
            this.alreadyComputedContext[state.getPos()][state.getFloor()][state.getDirection()] = [];
        }
        if (this.alreadyComputedContext[state.getPos()][state.getFloor()][state.getDirection()][state.getNbAdditionalElevators()] == null
            || this.alreadyComputedContext[state.getPos()][state.getFloor()][state.getDirection()][state.getNbAdditionalElevators()] > pathLength) {
            this.alreadyComputedContext[state.getPos()][state.getFloor()][state.getDirection()][state.getNbAdditionalElevators()] = pathLength;
            return true;
        } else {
            return false;
        }
    };

    this.printAllActions = function (stack) {
        while (stack.length > 0) {
            var state = stack.shift();
            debug("Prog   Pos " + state.state.getFloor() + ' ' + state.state.getPos() + ' ' + state.state.getDirection(), true);
            switch (state.action) {
                case ACTION_BUILTIN_ELEVATOR:
                    debug('[BUILTIN_ELEVATOR]', true);
                    printOut(ACTION_WAIT, true);
                    break;
                case ACTION_WAIT:
                    debug('[WAIT]', true);
                    printOut(ACTION_WAIT, true);
                    break;
                case ACTION_BLOCK:
                    debug('[BLOCK + 2 wait]', true);
                    printOut(ACTION_BLOCK, true);
                    printOut(ACTION_WAIT, true);
                    printOut(ACTION_WAIT, true);
                    break;
                case ACTION_ELEVATOR:
                    debug('[ELEVATOR + 3 Wait]', true);
                    printOut(ACTION_ELEVATOR, true);
                    printOut(ACTION_WAIT, true);
                    printOut(ACTION_WAIT, true);
                    printOut(ACTION_WAIT, true);
                    break;
            }
        }
        // One last step to go
        printOut(ACTION_WAIT);
        printOut(ACTION_WAIT);
    };

    this.nextAction = function (currentContext) {
        //noinspection FallThroughInSwitchStatementJS
        switch (currentContext.action) {
            case null:
                if (currentContext.state.getNbAdditionalElevators() > 0) {
                    return ACTION_ELEVATOR;
                }
            case ACTION_ELEVATOR:
                return ACTION_WAIT;
            case ACTION_WAIT:
                return ACTION_BLOCK;
            case ACTION_BLOCK :
                return null;
            case ACTION_BUILTIN_ELEVATOR:
                printOut('Stucked !!!!');
                throw 'shouldn\'t happen because BUILTIN ELEVATAR is handle elsewhere';
                return null;
        }
    };

    this.getPreviousAction = function (stack) {
        return stack.length == 0 ? null : stack[stack.length - 1].action;
    };

    this.tryToDo = function (stack, context, actionToDo) {
        // Verify if there is enough round remaining
        if (this.computeStackLength(stack, actionToDo) >= configuration.nbRounds) {
            throw 'No more move to process';
        }

        try {
            var futureState = this.do(context.state, actionToDo, this.getPreviousAction(stack));
        } catch (err) {
            debug(err);
        }

        try {
            this.levelMap.getContent(futureState.getPos(), futureState.getFloor()); // Will throw error if illegalPosition
        } catch (err) {
            debug('Can\'t do [' + actionToDo + '] at [' + context.state.getPos() + '][' + context.state.getFloor() + '][' + context.state.getDirection() + ']');
            throw err;
        }

        // Index context and verify if a better context has already been found for this state
        if (!this.indexAndCheckState(futureState, stack.length)) {
            throw 'We have already try this path';
        }

        context.setAction(actionToDo);
        stack.push(context);
        return new Context(futureState, null);
    };

    this.do = function (state, actionToDo, previousAction) {
        var futureState;
        switch (actionToDo) {
            case ACTION_WAIT:
                switch (state.getDirection()) {
                    case DIRECTION_LEFT:
                        futureState = new State(state.getPos() - 1, state.getFloor(), state.getDirection(), state.getNbAdditionalElevators());
                        break;
                    case DIRECTION_RIGHT:
                        futureState = new State(state.getPos() + 1, state.getFloor(), state.getDirection(), state.getNbAdditionalElevators());
                        break;
                }
                break;
            case ACTION_BUILTIN_ELEVATOR:
                futureState = new State(state.getPos(), state.getFloor() + 1, state.getDirection(), state.getNbAdditionalElevators());
                break;
            case ACTION_ELEVATOR:
                futureState = new State(state.getPos(), state.getFloor() + 1, state.getDirection(), state.getNbAdditionalElevators() - 1);
                break;
            case ACTION_BLOCK:
                var newPos = (previousAction == ACTION_BUILTIN_ELEVATOR || previousAction == ACTION_ELEVATOR) ? state.getPos() : state.getDirection() == DIRECTION_LEFT ? state.getPos() + 2 : state.getPos() - 2;
                futureState = new State(newPos, state.getFloor(), state.getDirection() == DIRECTION_LEFT ? DIRECTION_RIGHT : DIRECTION_LEFT, state.getNbAdditionalElevators());
        }
        return futureState;
    }
}

function State(pos, floor, direction, nbAdditionalElevators) {
    return {
        representationStr : '['+pos+']['+floor+']['+direction+']['+nbAdditionalElevators+']',
        getPos: function () {
            return pos;
        },
        getFloor: function () {
            return floor;
        },
        getDirection: function () {
            return direction;
        },
        getNbAdditionalElevators: function () {
            return nbAdditionalElevators;
        }
    };
}

function Context(state, action) {
    this.state = state;
    this.action = action;

    this.setAction = function (action) {
        this.action = action;
    };

    this.getState = function () {
        return this.state;
    };

    this.getAction = function () {
        return this.action;
    };
}

// region Tests
function testAll() {
    // Add nodeJs assertions tools
    var assert = require('assert');

    var configLvlTest;

    function beforeTest() {
        configLvlTest = JSON.parse(lvlTest);
    }

    function testMove() {
        beforeTest();
        var marvin;

        var newState;
        marvin = new Marvin(configLvlTest);
        marvin.startingState = new State(5,0, DIRECTION_LEFT,0);
        newState = marvin.do(marvin.startingState, ACTION_WAIT);
        assert.equal(newState.getPos(), 4, 'New pos should be 4');

        marvin = new Marvin(configLvlTest);
        marvin.startingState = new State(5,0, DIRECTION_RIGHT,0);
        newState = marvin.do(marvin.startingState, ACTION_WAIT);
        assert.equal(newState.getPos(), 6, 'New pos should be 6');

        marvin = new Marvin(configLvlTest);
        newState = marvin.do(marvin.startingState, ACTION_ELEVATOR);
        assert.equal(newState.getFloor(), 1, 'New floor should be 1');

        marvin = new Marvin(configLvlTest);
        newState = marvin.do(marvin.startingState, ACTION_BLOCK);
        assert.equal(newState.getDirection(), DIRECTION_LEFT, 'New direction should be LEFT');
    }

    testMove();
}
//endregion Tests

// region Tools
/**
 * Return WEB or SIMU based on the presence of 'console'
 */
function getEnv() {
    //return TEST;
    if (typeof console !== 'undefined') {
        return SIMU;
    } else {
        return WEB;
    }
}

function debug(toDebug, simpleText) {
    if (simpleText == undefined || simpleText == false) {
        toDebug = JSON.stringify(toDebug);
    }
    if (getEnv() == SIMU || getEnv() == TEST) {
        console.log(toDebug);
    } else {
        //printErr(toDebug);
    }
}

function printOut(toPrint, simpleText) {
    if (getEnv() == SIMU || getEnv() == TEST) {
        if (simpleText == undefined || simpleText == false) {
            toPrint = JSON.stringify(toPrint);
        }
        process.stdout.write("out:" + toPrint + "\n");
    } else {
        print(toPrint);
        var inputs = readline().split(' ');
        var cloneFloor = parseInt(inputs[0]); // floor of the leading clone
        var clonePos = parseInt(inputs[1]); // position of the leading clone on its floor
        var direction = inputs[2];
        debug("CurrentPos " + cloneFloor + ' ' + clonePos + ' ' + direction, true);
    }
}

function LevelMap(width, height) {
    this.content = {};

    for (var i = 0; i < width; i++) {
        this.content[i] = [];
    }

    this.setContent = function (x, y, value) {
        this.content[x][y] = value;
    };

    this.getContent = function (x, y) {
        if (x < 0 || x > width - 1 || y < 0 || y > height - 1) {
            throw new Error('Index out of bound Exception');
        }
        return this.content[x][y] != null ? this.content[x][y] : '_';
    };

    this.print = function () {
        for (var y = height - 1; y >= 0; y--) {
            var level = '';
            for (var x = 0; x < width; x++) {
                level += ' ' + this.getContent(x, y) + ' ';
            }
            debug('|' + level + '|');
        }
    }
}
//endregion Tools