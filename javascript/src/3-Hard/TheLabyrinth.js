var inputs = readline().split(' ');
var R = parseInt(inputs[0]); // number of rows.
var C = parseInt(inputs[1]); // number of columns.
var A = parseInt(inputs[2]); // number of rounds between the time the alarm countdown is activated and the time the alarm goes off.

var map = new Map(C, R);
var kirk = new Kirk(map);

var debug, simpleDebug;

// game loop
var stepMax = 1600;
var step = 0;
while (step++ < stepMax) {
    var isDebugActivated = step < 0;
    debug = configDebug(isDebugActivated);
    simpleDebug = configSimpleDebug(isDebugActivated);
    inputs = readline().split(' ');
    var KR = parseInt(inputs[0]); // row where Kirk is located.
    var KC = parseInt(inputs[1]); // column where Kirk is located.
    kirk.setCoordinate(new Coord(KC, KR));

    map.reset();
    for (var i = 0; i < R; i++) {
        var ROW = readline(); // C of the characters in '#.TC?' (i.e. one line of the ASCII maze).
        map.pushLine(ROW);
    }

    var dir = kirk.computeDirection();
    print(dir.getValue());
}

function Kirk(map) {
    this.map = map;
    this.coord = null;
    this.path = [];
    this.controlCoordinate = null;
    this.pathToControl = [];

    var STATE_SEARCHING_CONTROL = 0;
    var STATE_GOING_TO_CONTROL = 1;
    var STATE_GOING_TO_TELEPORT = 2;

    var state = STATE_SEARCHING_CONTROL;

    this.setCoordinate = function (newCoord) {
        // Si le content est déjà setté c'est qu'on revient sur nos pas donc rien à faire
        if (this.map.getVisitedMapCellContent(newCoord) == 0) {
            this.map.visit(newCoord);
        }
        this.coord = newCoord;
    };

    this.computeDirection = function () {
        this.map.printVisitedMapOverlay(this.coord);
        if ((state == STATE_SEARCHING_CONTROL || state == STATE_GOING_TO_CONTROL) && this.map.getMapCellContent(this.coord) == 'C') {
            state = STATE_GOING_TO_TELEPORT;
        } else if (this.isItTimeToComeBackToControl()) {
            state = STATE_GOING_TO_CONTROL;
        }

        switch (state) {
            default :
            case STATE_SEARCHING_CONTROL:
                debug('Exploring');
                var directionToGo = kirk.explore();
                if (this.controlCoordinate != null) {
                    // Memorizing path to comeBack to control
                    this.pathToControl.push(directionToGo);
                }
                return directionToGo;
            case STATE_GOING_TO_CONTROL :
                debug('Going back to control');
                debug(this.pathToControl);
                var previousDir = this.pathToControl.pop();

                return previousDir.reverse();
            case STATE_GOING_TO_TELEPORT :
                debug("Coming back");
                return this.map.findAdjacentMoins1Dir(this.coord);
        }
    };

    this.explore = function explore(directionToAvoid) {
        var nextDirection;
        if ((nextDirection = this.getPathUnexplored(directionToAvoid)) == null) {
            // Reviens au précédent path pour essayer sur les côté
            var previousDir = this.path.pop();
            return previousDir.reverse();
        }
        if (this.mustPreventGoingToControl(this.map.getVisitedMapCellContent(this.coord) <= A, nextDirection)) {
            return this.explore(nextDirection);
        }
        this.path.push(nextDirection);
        return nextDirection;
    };

    // Return la direction d'un chemin inexploré autour de soi
    // Retourne le chemin vers le control que si la distance pour revenir est compatible
    this.getPathUnexplored = function (directionToAvoid) {
        var coordinate = this.coord;
        var that = this;
        // Pour chaque direction on cherche si la map est déjà visité
        var direction = new Direction();
        direction = direction.each(function (otherDir) {
            if (directionToAvoid != null && otherDir.equals(directionToAvoid)) {
                return;
            }
            var nextCoordinate = coordinate.apply(otherDir);
            if (!that.map.isVisited(nextCoordinate) && !that.map.isAWall(nextCoordinate)) {
                return otherDir;
            }
        });
        return direction;
    };

    // Vérifie si la mise à jour du chemin à permis de trouver un chemin pour aller de Ctrl > Téléport dans le temps impartit
    this.isItTimeToComeBackToControl = function () {
        var that = this;
        if (this.controlCoordinate == null) {
            return false;
        }
        if (this.map.nodeValuesRecomputed) {
            var goodToGo = false;
            this.controlCoordinate.eachAdjacent(function (coordinate) {
                debug(coordinate);
                debug(that.map.getVisitedMapCellContent(coordinate));
                if (that.map.getVisitedMapCellContent(coordinate) == A) {
                    goodToGo = true;
                }
            });
            this.map.nodeValuesRecomputed = false;
            return goodToGo;
        }
        return false;
    };

    // Vérifie si coordToGo est le 'C' si tel est le cas on vérifie que currentCoord = A + 1
    this.mustPreventGoingToControl = function (stepCountOK, nextDirection) {
        if (stepCountOK) {
            return false;
        }
        var coordinateToCheck = this.coord.apply(nextDirection);
        if (this.map.getMapCellContent(coordinateToCheck) == 'C') {
            this.controlCoordinate = coordinateToCheck;
            this.pathToControl.push(nextDirection.reverse());
            return true;
        }
        return false;
    }
}

function Direction(value) {

    var directions = ['RIGHT', 'DOWN', 'LEFT', 'UP'];

    if (value == null) {
        value = directions[0];
    }

    var index = directions.indexOf(value);
    if (index == -1) {
        throw "Direction unknown " + value;
    }

    this.getValue = function () {
        return value;
    };

    this.equals = function (otherDir) {
        return value == otherDir.getValue();
    };

    this.reverse = function () {
        return new Direction(directions[(index + 2) % 4]);
    };

    this.next = function (skipDir) {
        var nextDirectionIndex = (index + 1) % 4;
        var nextDirection = new Direction(directions[nextDirectionIndex]);
        if (skipDir == null) {
            return nextDirection;
        }
        return nextDirection.equals(skipDir) ? nextDirection.next() : nextDirection;
    };

    this.each = function (callback) {
        for (var i = 0; i < 4; i++) {
            var toReturn;
            if ((toReturn = callback(new Direction(directions[(index + i) % 4]))) != null) {
                return toReturn;
            }
        }
    };

    this.getModifierX = function () {
        if (value == "LEFT") {
            return -1;
        }
        if (value == "RIGHT") {
            return +1;
        }
        return 0;
    };

    this.getModifierY = function () {
        if (value == "UP") {
            return -1;
        }
        if (value == "DOWN") {
            return +1;
        }
        return 0;
    };
}

function pad(str, pad) {
    return (pad + str).slice(-pad.length)
}

function Map(sizeWidth, sizeHeight) {
    this.map = [];
    this.visitedMap = [];
    this.nodeValuesRecomputed = false;

    for (var i = 0; i < sizeHeight; i++) {
        this.visitedMap[i] = [];
        for (var j = 0; j < sizeWidth; j++) {
            this.visitedMap[i][j] = 0;
        }
    }

    this.reset = function () {
        this.map = [];
    };

    this.pushLine = function (row) {
        this.map.push(row);
    };

    this.printMap = function () {
        for (var rowKey in this.map) {
            simpleDebug(this.map[rowKey]);
        }
    };

    this.printVisitedMap = function () {
        for (var rowKey in this.visitedMap) {
            simpleDebug(this.visitedMap[rowKey]);
        }
    };

    this.printVisitedMapOverlay = function (coord) {
        for (var rowKey in this.map) {
            var line = '';
            for (var cellKey in this.map[rowKey]) {
                var cellValue = this.map[rowKey][cellKey] == '.' ? pad(this.visitedMap[rowKey][cellKey], '...') : pad(this.map[rowKey][cellKey], '...');
                if (coord != null && rowKey == coord.y && cellKey == coord.x) {
                    cellValue = 'SPK';
                }
                line += cellValue;
            }
            simpleDebug(line);
        }
    };

    this.getCellContent = function (map, coordinate) {
        return map[coordinate.y][coordinate.x];
    };

    this.getMapCellContent = function (coordinate) {
        return this.getCellContent(this.map, coordinate);
    };

    this.getVisitedMapCellContent = function (coordinate) {
        return this.getCellContent(this.visitedMap, coordinate);
    };

    this.visit = function (coord) {
        this.computeRealDistanceAndPropagate(coord);
    };

    this.findAdjacentMoins1Dir = function (coordinate) {
        var that = this;
        var currentValue = this.getVisitedMapCellContent(coordinate);
        var dirToGo = null;
        coordinate.eachAdjacent(function (adjacent, dir) {
            if (dirToGo != null) {
                return;
            }
            var adjacentValue = that.getVisitedMapCellContent(adjacent);

            if (adjacentValue == currentValue - 1) {
                dirToGo = dir;
            }
        });
        if (dirToGo == null) {
            throw 'No adjacent has a value equals at mine minus one';
        }
        return dirToGo;
    };

    this.computeRealDistanceAndPropagate = function (coordinate) {
        var that = this;

        var currentValue = null;
        var minimumDesCasesAdjacentes = null;

        // On regarde autour le min > 0 pour savoir notre valeur
        coordinate.eachAdjacent(function (adjacent) {
            var adjacentValue = that.getVisitedMapCellContent(adjacent);
            if (adjacentValue == 0) {
                return;
            }
            if (minimumDesCasesAdjacentes == null) {
                minimumDesCasesAdjacentes = adjacentValue;
            } else {
                if (minimumDesCasesAdjacentes > adjacentValue) {
                    //debug('autresPlusGros' + adjacentValue);
                    minimumDesCasesAdjacentes = adjacentValue;
                }
            }
        });

        currentValue = minimumDesCasesAdjacentes + 1;
        this.visitedMap[coordinate.y][coordinate.x] = currentValue;

        // pour chaque noeud adjacent
        coordinate.eachAdjacent(function (adjacent) {
            // Si une case adjacente est > à moi + 1 je la mets à jour
            var adjacentValue = that.getVisitedMapCellContent(adjacent);
            if (adjacentValue > currentValue + 1) { // Case non visitée
                that.nodeValuesRecomputed = true;
                that.computeRealDistanceAndPropagate(adjacent);
            }
        });
    };

    this.isVisited = function (coord) {
        return this.visitedMap[coord.y][coord.x] != 0;
    };

    this.isAWall = function (coord) {
        return this.map[coord.y][coord.x] == '#';
    }
}

function Coord(x, y) {
    this.x = x;
    this.y = y;

    this.apply = function (direction) {
        return new Coord(this.x + direction.getModifierX(), this.y + direction.getModifierY());
    };

    this.eachAdjacent = function (callback) {
        var dir = new Direction('UP');
        for (var i = 0; i < 4; i++) {
            dir = dir.next();
            callback(this.apply(dir), dir);
        }
    }
}

function configSimpleDebug(activated) {
    if (activated) {
        return function (value) {
            printErr(value);
        };
    } else {
        return function () {
        };
    }
}

function configDebug(activated) {
    if (!activated) {
        return function (value) {
        };
    } else {
        return function (value) {
            printErr(JSON.stringify(value));
        }
    }
}