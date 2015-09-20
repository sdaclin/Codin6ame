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
const lvlTest = '{"nbFloors":3,"width":10,"nbRounds":6,"exitFloor":2,"exitPos":2,"nbTotalClones":4,"nbAdditionalElevators":1,"nbElevators":1,"elevators":[],"marvinStartingState":{"floor":0,"pos":5,"direction":"RIGHT"}}';

const lvl1 = '{"nbFloors":2,"width":13,"nbRounds":100,"exitFloor":1,"exitPos":11,"nbTotalClones":10,"nbAdditionalElevators":1,"nbElevators":0,"elevators":[],"marvinStartingState":{"floor":0,"pos":2,"direction":"RIGHT"}}';
const lvl3 = '{"nbFloors":6,"width":13,"nbRounds":100,"exitFloor":5,"exitPos":10,"nbTotalClones":10,"nbAdditionalElevators":5,"nbElevators":0,"elevators":[],"marvinStartingState":{"floor":0,"pos":1,"direction":"RIGHT"}}';
const lvl5 = '{"nbFloors":7,"width":13,"nbRounds":30,"exitFloor":6,"exitPos":7,"nbTotalClones":10,"nbAdditionalElevators":3,"nbElevators":3,"elevators":[{"elevatorFloor":2,"elevatorPos":6},{"elevatorFloor":0,"elevatorPos":6},{"elevatorFloor":3,"elevatorPos":7}],"marvinStartingState":{"floor":0,"pos":4,"direction":"RIGHT"}}';

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
    configuration = JSON.parse(lvl5);
}

var marvin = new Marvin(configuration);
var stackToGo = marvin.computeSolution();
marvin.printAllActions(stackToGo);

function Marvin(configuration) {
    var that = this;
    this.startingState = new State(configuration.marvinStartingState.pos, configuration.marvinStartingState.floor, configuration.marvinStartingState.direction, configuration.nbAdditionalElevators);

    // Init LevelMap
    this.levelMap = new LevelMap(configuration.width, configuration.nbFloors);
    configuration.elevators.forEach(function (elevator) {
        that.levelMap.setContent(elevator.elevatorPos, elevator.elevatorFloor, MAP_ELEVATOR);

    });
    this.levelMap.setContent(configuration.exitPos, configuration.exitFloor, MAP_EXIT);
    this.levelMap.setContent(configuration.marvinStartingState.pos, configuration.marvinStartingState.floor, MAP_START);
    this.levelMap.print();

    this.computeSolution = function () {
        var stack = []; // stackToGoToExit
        var alreadyComputedContext = [];
        var currentContext = new Context(this.startingState, null);
        var wayToExitIsFound = false;
        while (!wayToExitIsFound) {
            //debug(stack);
            if (stack.length > 0) {
                debug(stack[stack.length - 1]);
            }
            var that = this;

            var currentContent = this.levelMap.getContent(currentContext.state.pos, currentContext.state.floor);
            if (currentContent == MAP_EXIT) {
                wayToExitIsFound = true;
            } else if (currentContent == MAP_ELEVATOR) {
                nextAction = ACTION_BUILTIN_ELEVATOR;
                futureState = this.do(currentContext.state, ACTION_BUILTIN_ELEVATOR);
                currentContext.setAction(ACTION_BUILTIN_ELEVATOR);
                stack.push(currentContext);
                currentContext = new Context(futureState, null);
            } else {
                var nextAction;
                if ((nextAction = this.nextAction(currentContext)) != null) {
                    var futureState;
                    try {
                        futureState = that.do(currentContext.getState(), nextAction);
                        currentContext.setAction(nextAction);
                        // Index context and verify if a better context has already been found for this state
                        if (!this.indexAndCheckState(alreadyComputedContext, futureState)) {
                            //currentContext = stack.pop();
                            continue;
                        }
                        stack.push(currentContext);
                        currentContext = new Context(futureState, null);
                    } catch (err) {
                        debug('Can\'t do [' + nextAction + '] at [' + currentContext.getState().pos + '][' + currentContext.getState().floor + '][' + currentContext.getState().direction + ']');
                        currentContext = stack.pop();
                    }
                } else {
                    // no more action to try
                    do {
                        currentContext = stack.pop();
                    } while (currentContext.action == ACTION_BUILTIN_ELEVATOR);
                }
            }
        }

        debug(stack);
        return stack;
    };

    this.indexAndCheckState = function (alreadyComputedContext, state) {
        if (alreadyComputedContext[state.pos] == null) {
            alreadyComputedContext[state.pos] = [];
        }
        if (alreadyComputedContext[state.pos][state.floor] == null) {
            alreadyComputedContext[state.pos][state.floor] = [];
        }
        if (alreadyComputedContext[state.pos][state.floor][state.direction] == null || alreadyComputedContext[state.pos][state.floor][state.direction] < state.nbAdditionalElevators) {
            alreadyComputedContext[state.pos][state.floor][state.direction] = state.nbAdditionalElevators;
            return true;
        } else {
            return false;
        }
    };

    this.printAllActions = function (stack) {
        while (stack.length > 0) {
            var state = stack.shift();
            switch (state.action) {
                case ACTION_BUILTIN_ELEVATOR:
                    debug('\tBUILTIN_ELEVATOR + 1 wait');
                    printOut(ACTION_WAIT);
                    break;
                case ACTION_WAIT:
                    debug('\tWAIT');
                    printOut(ACTION_WAIT);
                    break;
                case ACTION_BLOCK:
                    debug('\tBLOCK + 2 wait');
                    printOut(ACTION_BLOCK);
                    printOut(ACTION_WAIT);
                    printOut(ACTION_WAIT);
                    break;
                case ACTION_ELEVATOR:
                    debug('\tELEVATOR + 3 Wait');
                    printOut(ACTION_ELEVATOR);
                    printOut(ACTION_WAIT);
                    printOut(ACTION_WAIT);
                    printOut(ACTION_WAIT);
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
                if (currentContext.state.nbAdditionalElevators > 0) {
                    return ACTION_ELEVATOR;
                }
            case ACTION_ELEVATOR:
                return ACTION_WAIT;
            case ACTION_WAIT:
            case ACTION_BUILTIN_ELEVATOR: // the two are the same because builtin elevator is applied automatically
                return ACTION_BLOCK;
            case ACTION_BLOCK :
                return null;
        }
    };

    this.do = function (state, actionToDo) {
        var futureState;
        switch (actionToDo) {
            case ACTION_WAIT:
                switch (state.direction) {
                    case DIRECTION_LEFT:
                        futureState = new State(state.pos - 1, state.floor, state.direction, state.nbAdditionalElevators);
                        break;
                    case DIRECTION_RIGHT:
                        futureState = new State(state.pos + 1, state.floor, state.direction, state.nbAdditionalElevators);
                        break;
                }
                break;
            case ACTION_BUILTIN_ELEVATOR:
                futureState = new State(state.pos, state.floor + 1, state.direction, state.nbAdditionalElevators);
                break;
            case ACTION_ELEVATOR:
                futureState = new State(state.pos, state.floor + 1, state.direction, state.nbAdditionalElevators - 1);
                break;
            case ACTION_BLOCK:
                futureState = new State(state.pos = state.direction == DIRECTION_LEFT ? state.pos + 2 : state.pos - 2, state.floor, state.direction == DIRECTION_LEFT ? DIRECTION_RIGHT : DIRECTION_LEFT, state.nbAdditionalElevators);
        }
        this.levelMap.getContent(futureState.pos, futureState.floor); // Will throw error if illegalPosition
        return futureState;
    }
}

function State(pos, floor, direction, nbAdditionalElevators) {
    this.pos = pos;
    this.floor = floor;
    this.direction = direction;
    this.nbAdditionalElevators = nbAdditionalElevators;
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

        var movedMarvin;
        marvin = new Marvin(configLvlTest, 5, 0, DIRECTION_LEFT);
        movedMarvin = marvin.do(ACTION_WAIT);
        assert.equal(movedMarvin.startingState.pos, 4, 'New pos should be 4');

        marvin = new Marvin(configLvlTest, 5, 0, DIRECTION_RIGHT);
        movedMarvin = marvin.do(ACTION_WAIT);
        assert.equal(movedMarvin.startingState.pos, 6, 'New pos should be 6');

        marvin = new Marvin(configLvlTest, 5, 0, DIRECTION_RIGHT);
        movedMarvin = marvin.do(ACTION_ELEVATOR);
        assert.equal(movedMarvin.startingState.floor, 1, 'New floor should be 1');

        marvin = new Marvin(configLvlTest, 5, 0, DIRECTION_RIGHT);
        movedMarvin = marvin.do(ACTION_BLOCK);
        assert.equal(movedMarvin.startingState.direction, DIRECTION_LEFT, 'New direction should be LEFT');
    }

    testMove();

    function testInvalidMoves() {
        beforeTest();
        var marvin;

        marvin = new Marvin(configLvlTest, 0, 0, DIRECTION_LEFT);
        assert.throws(function () {
            marvin.do(ACTION_WAIT)
        }, /out of bound/, 'An exception should be thrown when marvin try to go out of the floor');

        marvin = new Marvin(configLvlTest, 0, 2, DIRECTION_RIGHT);
        assert.throws(function () {
            marvin.do(ACTION_ELEVATOR)
        }, /out of bound/, 'An exception should be thrown when marvin try to go up when on last floor');
    }

    testInvalidMoves();
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

function debug(toDebug) {
    toDebug = JSON.stringify(toDebug);
    if (getEnv() == SIMU || getEnv() == TEST) {
        console.log(toDebug);
    } else {
        printErr(toDebug);
    }
}

function printOut(toPrint) {
    if (getEnv() == SIMU || getEnv() == TEST) {
        toPrint = JSON.stringify(toPrint);
        process.stdout.write(toPrint + "\n");
    } else {
        print(toPrint);
        var inputs = readline().split(' ');
        var cloneFloor = parseInt(inputs[0]); // floor of the leading clone
        var clonePos = parseInt(inputs[1]); // position of the leading clone on its floor
        var direction = inputs[2];
        debug(cloneFloor + ' ' + clonePos + ' ' + direction);
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