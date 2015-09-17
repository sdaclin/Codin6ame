var conf = {bikes: []};
var web = true;
if (web) {
    conf.M = parseInt(readline()); // the amount of motorbikes to control
    conf.V = parseInt(readline()); // the minimum amount of motorbikes that must survive

    var road = new Road();
    var resistanceApp = new ResistanceApp(road, conf.M, conf.V);
    conf.L0 = readline(); // L0 to L3 are lanes of the road. A dot character . represents a safe space, a zero 0 represents a hole in the road.
    road.addLine(new Lane(conf.L0));
    conf.L1 = readline();
    road.addLine(new Lane(conf.L1));
    conf.L2 = readline();
    road.addLine(new Lane(conf.L2));
    conf.L3 = readline();
    road.addLine(new Lane(conf.L3));

    // game loop
    var step = -1;
    var actions = [];
    while (step++ >= -1) {
        resistanceApp.clearBikes();
        S = parseInt(readline()); // the motorbikes' speed
        for (var i = 0; i < conf.M; i++) {
            var inputs = readline().split(' ');
            X = parseInt(inputs[0]); // x coordinate of the motorbike
            Y = parseInt(inputs[1]); // y coordinate of the motorbike
            A = parseInt(inputs[2]); // indicates whether the motorbike is activated "1" or destroyed "0"
            conf.bikes[i] = new Bike(X, Y, S, A);
            resistanceApp.addBike(conf.bikes[i]);
        }
        if (step == 0) {
            debug(conf);
        }

        if (step == 0) {
            resistanceApp.driveThrewRoad(resistanceApp.bikes, actions);
        }

        print(resistanceApp.getNextActionToPerform(actions).action);
    }
} else {
    var confStr1 = '{"bikes":[{"x":0,"y":2,"speed":0,"isActivated":1}],"M":1,"L0":"..............................","L1":"..............................","L2":"...........0..................","L3":"..............................","V":1}';
    var confStr2 = '{"bikes":[{"x":0,"y":0,"speed":1,"isActivated":1,"isJumping":false},{"x":0,"y":1,"speed":1,"isActivated":1,"isJumping":false},{"x":0,"y":2,"speed":1,"isActivated":1,"isJumping":false},{"x":0,"y":3,"speed":1,"isActivated":1,"isJumping":false}],"M":4,"V":4,"L0":"..........000......0000..............000000.............","L1":"..........000......0000..............000000.............","L2":"..........000......0000..............000000.............","L3":"..........000......0000..............000000............."}';
    var confStr3 = '{"bikes":[{"x":0,"y":0,"speed":8,"isActivated":1,"isJumping":false},{"x":0,"y":1,"speed":8,"isActivated":1,"isJumping":false},{"x":0,"y":2,"speed":8,"isActivated":1,"isJumping":false},{"x":0,"y":3,"speed":8,"isActivated":1,"isJumping":false}],"M":4,"V":4,"L0":"..............00000......0000.....00......","L1":"..............00000......0000.....00......","L2":"..............00000......0000.....00......","L3":"..............00000......0000.....00......"}';
    launchApp(confStr3);
    function launchApp(confStr) {
        var conf = JSON.parse(confStr);
        var road = new Road();
        var resistanceApp = new ResistanceApp(road, conf.M, conf.V);
        road.addLine(new Lane(conf.L0));
        road.addLine(new Lane(conf.L1));
        road.addLine(new Lane(conf.L2));
        road.addLine(new Lane(conf.L3));

        // game loop
        var step = -1;
        var actions = [];
        //while (step++ < 50) {
        resistanceApp.clearBikes();
        for (var i = 0; i < conf.M; i++) {
            resistanceApp.addBike(conf.bikes[i]);
        }

        //if (step == 0) {
        resistanceApp.driveThrewRoad(resistanceApp.bikes, actions);
        //}

        while (actions.length > 0) {
            console.log(resistanceApp.getNextActionToPerform(actions));
        }
        //}
    }
}

function ResistanceApp(road, nbBike, nbBikeToSurvive) {
    this.road = road;
    this.bikes = [];

    this.clearBikes = function () {
        this.bikes = [];
    };

    this.addBike = function (bike) {
        this.bikes.push(bike);
    };

    this.getNextActionToPerform = function (actions) {
        return actions.shift();
    };

    this.debugRoadAndBikes = function (bikes) {
        var that = this;
        var toPrint = '';
        this.road.lanes.forEach(function (lane, laneIdx) {
            var laneToPrint = lane.lane.slice(0);
            bikes.forEach(function (bike, bikeIdx) {
                if (bike.y == laneIdx) {
                    laneToPrint[bike.x] = 'b';
                }
            });
            debug(laneToPrint.join(''));
        })
    };

    this.driveThrewRoad = function (bikes, actions) {
        var that = this;

        if (bikes[0].isActivated == false) {
            debug(bikes[0]);
        }

        // Tant qu'on n'est pas à la fin
        if (bikes[0].x > this.road.lanes[0].lane.length) {
            return 'Path found';
        }
        debug("CurrentSpeed:" + bikes[0].speed);
        this.debugRoadAndBikes(bikes);

        // On essaie toutes les actions possibles
        var action = new Action("WAIT");
        var result = null;
        action.forEach(function (action) {
            if (result != null) {
                return;
            }
            if (bikes[0].speed == 0 && action.action != 'SPEED') {
                return;
            }
            if ((bikes[0].y == 0 && action.action == 'UP') || (bikes[bikes.length - 1].y == 3 && action.action == 'DOWN')) {
                return;
            }
            debug("Trying to [" + action.action + "] at [" + bikes[0].x + "]");
            var resultBikes = [];
            var countDesactivated = 0;
            var canBreak = false;
            bikes.forEach(function (bike) {
                if (canBreak) {
                    return;
                }
                var resultBike = that.applyAction(bike, action);
                if (resultBike.isActivated) {
                    resultBikes.push(resultBike);
                } else {
                    debug('One bike is lost while doing [' + action.action + '] at [' + bike.x + ',' + bike.y + ']');
                    countDesactivated++;
                    if (nbBike - countDesactivated < nbBikeToSurvive) {
                        canBreak = true;
                    }
                }
            });
            // S'il y a assez de moto, on passe à l'étape suivante sinon on revient à l'étape précédente
            if (resultBikes.length >= nbBikeToSurvive) {
                actions.push(action);
                result = that.driveThrewRoad(resultBikes, actions);
                if (result == null) {
                    // Reverting curent action because it doesn't lead to a good issue
                    debug("Action [" + action.action + "] at [" + bikes[0].x + "] is not a good option");
                    actions.pop();
                }
            }
        });
        return result;
    };

    this.applyAction = function (bike, action) {
        var that = this;
        var newBike = new Bike(bike.x, bike.y, bike.speed, bike.isActivated);

        var move;
        switch (action.action) {
            case 'SPEED':
                newBike.speed++;
                if (newBike.speed > 50) {
                    newBike.speed = 50;
                }
                move = 'WAIT';
                break;
            case 'SLOW':
                newBike.speed--;
                if (newBike.speed < 0) {
                    newBike.speed = 0;
                }
                move = 'WAIT';
                break;
            default:
                move = action.action;
                break;
        }

        var newCoordCallback = function (x, y) {
            if (that.road.getCell(x, y) == '0') {
                throw new OverAHoleException();
            }
        };
        try {
            switch (move) {
                case 'JUMP':
                    newBike.startJump();
                    newBike.forward(newCoordCallback);
                    break;
                case 'WAIT':
                    newBike.forward(newCoordCallback);
                    break;
                case 'UP':
                case'DOWN':
                    newBike.upDownAndForward(move, newCoordCallback);
                    break;
            }
        } catch (e) {
            if (e instanceof OverAHoleException) {
                newBike.isActivated = false;
                return newBike;
            }
        }
        return newBike;
    };
}

function OverAHoleException() {
}

function Lane(content) {
    this.lane = content.split('');

    this.toString = function () {
        return this.lane.join('');
    }
}

function Road() {
    this.lanes = [];

    this.addLine = function (lane) {
        this.lanes.push(lane);
    };

    this.toString = function () {
        var result = '';
        this.lanes.forEach(function (lane) {
            result += lane.toString() + '\n';
        });
        return result;
    };

    this.debug = function () {
        this.toString().split('\n').forEach(function (lane) {
            debug(lane);
        })
    };

    this.getCell = function (x, y) {
        return this.lanes[y].lane[x];
    }
}

function Bike(x, y, speed, isActivated) {
    this.x = x;
    this.y = y;
    this.speed = speed;
    this.isActivated = isActivated;
    this.isJumping = false;

    this.startJump = function () {
        this.isJumping = true;
    };

    this.forward = function (newCoordCallback) {
        for (var i = 0; i < this.speed; i++) {
            this.x++;
            if (!this.isJumping) {
                newCoordCallback(this.x, this.y);
            }
        }
        if (this.isJumping) {
            this.isJumping = false;
            newCoordCallback(this.x, this.y);
        }
    };

    this.upDownAndForward = function (direction, newCoordCallback) {
        var delta;
        switch (direction) {
            case 'UP':
                delta = -1;
                break;
            case 'DOWN':
                delta = +1;
                break;
        }
        var tempX;
        var tempY;
        // intermediate coords
        for (var i = 1; i < this.speed; i++) {
            tempX = this.x + i;
            tempY = this.y;
            newCoordCallback(tempX, tempY);
            tempY = this.y + delta;
            newCoordCallback(tempX, tempY);
        }
        // final coord
        this.x = this.x + this.speed;
        this.y = this.y + delta;
        newCoordCallback(this.x, this.y);
    };
}

function Action(action) {
    var actions = ['SPEED', 'SLOW', 'JUMP', 'WAIT', 'UP', 'DOWN'];
    if (actions.indexOf(action) == -1) {
        throw 'Action unknown';
    }
    this.action = action;

    this.forEach = function (callback) {
        actions.forEach(function (actionStr) {
            callback(new Action(actionStr));
        })
    };
}

function debugSimple(value) {
    printErr(value);
}

function debug(value) {
    if (typeof console !== 'undefined') {
        console.log(JSON.stringify(value));
    } else {
        printErr(JSON.stringify(value));
    }

}