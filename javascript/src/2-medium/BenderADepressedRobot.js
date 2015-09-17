var SOUTH = 0;
var EAST = 1;
var NORTH = 2;
var WEST = 3;

var inputs = readline().split(' ');
var L = parseInt(inputs[0]);
var C = parseInt(inputs[1]);

var map = new Map(C, L);
for (var i = 0; i < L; i++) {
    var row = readline();
    map.setLine(row);
}
map.enhance();

var blender = new Blender(map);
blender.init();
//debug(blender);
blender.walkAround();

blender.printResult();

// Write an action using print()
// To debug: printErr('Debug messages...');

function Coord(x, y) {
    this.x = x;
    thix.y = y;
}

function Blender(map) {
    this.map = map;
    this.startingPoint;
    this.endingPoint;
    this.currentDir;
    this.currentCoord;
    this.breakerMode = false;
    this.inverterMode = false;
    this.path = [];

    this.init = function () {
        // Find startingPoint en endingPoint
        this.startingPoint = map.find("@");
        //debug(this.startingPoint);
        this.currentCoord = this.startingPoint;
        this.map.visite(this.currentCoord);
        this.endingPoint = map.find("$");
        this.currentDir = null;
    }

    this.walkAround = function () {
        var currentMove = 0;
        var resetPriority = true;
        var forceNextDir = new Direction('SOUTH');
        var step = 0;
        while (!this.endingPoint.equals(this.currentCoord)) {
            //debug(this.currentCoord);

            // Found next coord
            var isNextCoordFound = false;
            var nextCoord = null;
            var nextCell = null;
            while (!isNextCoordFound) {
                if (forceNextDir != null) {
                    this.currentDir = forceNextDir.copy();
                    forceNextDir = null;
                }
                // Calculate next coord according to direction
                nextCoord = this.currentDir.applyDir(this.currentCoord);
                // Get next coor cell content
                nextCell = this.map.get(nextCoord);

                // Apply modifiers for nextCell
                if (nextCell == 'X' && this.breakerMode) {
                    debug("Modifier breakingCell");
                    nextCell = ' ';
                }
                if (nextCell == '#' || nextCell == 'X') {
                    if (resetPriority) {
                        if (!this.inverterMode) {
                            this.currentDir = new Direction('SOUTH');
                        } else {
                            this.currentDir = new Direction('WEST');
                        }
                        resetPriority = false;
                        continue;
                    }
                    this.currentDir = this.currentDir.nextDir(this.inverterMode);
                    continue;
                }
                isNextCoordFound = true;
            }

            // Move to nextCell
            this.path.push(this.currentDir);
            if (step++ > 50)
                debug({coord: this.currentCoord, dir: this.currentDir});
            this.currentCoord = nextCoord;
            if (this.map.isAlreadyVisited(this.currentCoord, this.currentDir, this.invertedMode, this.breakerMode)) {
                this.path = [];
                this.path.push('LOOP');
                return;
            }
            this.map.visite(this.currentCoord, this.currentDir, this.invertedMode, this.breakerMode);
            var currentCell = this.map.get(this.currentCoord);

            // Apply cell content rules
            resetPriority = true;
            //debug('currentCell                    '+currentCell);
            if (currentCell == 'X') {
                if (!this.breakerMode) {
                    throw "should be in breakerMode !";
                }
                this.map.changeCell(this.currentCoord, ' ');
            }
            if (currentCell == 'N') {
                debug('Modifier NOOOOOOOOOOOOOOOORTH');
                forceNextDir = new Direction('NORTH');
            }
            if (currentCell == 'S') {
                debug('Modifier SOUUUUUUUUUUUUUUUUUUUTH');
                forceNextDir = new Direction('SOUTH');
            }
            if (currentCell == 'W') {
                debug('Modifier WEEEEEEEEEEEEEEEEEEEST');
                forceNextDir = new Direction('WEST');
            }
            if (currentCell == 'E') {
                debug('Modifier EEEEEEEEEEEEEEEEEAST');
                forceNextDir = new Direction('EAST');
            }
            if (currentCell == 'B') {
                this.breakerMode = !this.breakerMode;
                debug("breakerMode" + this.breakerMode);
            }
            if (currentCell == 'I') {
                this.inverterMode = !this.inverterMode;
                debug("inverterMode" + this.inverterMode);
            }
            if (currentCell == 'T') {
                debug('Modifier TELEPOOOOOOOOOOORT');
                this.currentCoord = this.map.find('T', this.currentCoord);
            }
        }
    }

    this.printResult = function () {
        this.path.forEach(function (dir) {
            print(dir.toString());
        })
    }
}

function Map(width, height) {
    this.map = [];

    this.setLine = function (row) {
        this.map.push(row.split(''));
    };

    this.enhance = function () {
        for (var idx in this.map) {
            for (var idx1 in this.map[idx]) {
                this.map[idx][idx1] = {visited: [], content: this.map[idx][idx1]};
            }
        }
    }

    this.find = function (charToFind, coordToSkip) {
        var coord;
        this.map.forEach(function (line, y) {
            line.forEach(function (content, x) {
                var cell = content.content;
                if (cell == charToFind) {
                    var currentCoord = new Coord(x, y);
                    if (coordToSkip != null && coordToSkip.equals(currentCoord)) {
                        return;
                    }
                    coord = currentCoord;
                }
            })
        });
        return coord;
    }

    this.get = function (coord) {
        return this.map[coord.y][coord.x].content;
    }

    this.isAlreadyVisited = function (coord, currentDir, inverted, breaker) {
        var currentVisitConfig = new VisiteConfig(currentDir, inverted, breaker);
        return this.map[coord.y][coord.x].visited.reduce(function (previousResult, existingConfig) {
            return previousResult || currentVisitConfig.equals(existingConfig);
        }, false);
    }

    this.visite = function (coord, currentDir, inverted, breaker) {
        this.map[coord.y][coord.x].visited.push(new VisiteConfig(currentDir, inverted, breaker));
    }

    this.resetVisitedStates = function () {
        for (var idx in this.map) {
            for (var idx1 in this.map[idx]) {
                this.map[idx][idx1].visited = [];
            }
        }
    }

    this.changeCell = function (coord, newValue) {
        debug("CHANGING CELL");
        debug(coord);
        this.map[coord.y][coord.x].content = newValue;
        this.resetVisitedStates();
    }
}

function VisiteConfig(currentDir, inverted, breaker) {
    this.dir = currentDir;
    this.inverted = inverted;
    this.breaker = breaker;

    this.equals = function (otherConfig) {
        return this.dir.equals(otherConfig.dir)
            && this.inverted == otherConfig.inverted
            && this.breaker == otherConfig.breaker;
    }
}

function Coord(x, y) {
    this.x = x;
    this.y = y;

    this.equals = function (otherCoord) {
        return this.x == otherCoord.x && this.y == otherCoord.y;
    }
}

function Direction(dirStr) {
    this.dir = dirStr;

    this.applyDir = function (coord) {
        var modifierX = 0, modifierY = 0;
        switch (this.dir) {
            case 'SOUTH':
                modifierY = +1;
                break;
            case 'EAST':
                modifierX = +1;
                break;
            case 'NORTH':
                modifierY = -1;
                break;
            case 'WEST':
                modifierX = -1;
                break;
        }
        return new Coord(coord.x + modifierX, coord.y + modifierY);
    }

    this.nextDir = function (inverseEnabled) {
        switch (this.dir) {
            case 'SOUTH':
                return new Direction('EAST').inverse(inverseEnabled);
            case 'EAST':
                return new Direction('NORTH').inverse(inverseEnabled);
            case 'NORTH':
                debug("nextDir");
                debug(new Direction('WEST').inverse(inverseEnabled));
                return new Direction('WEST').inverse(inverseEnabled);
            case 'WEST':
                return new Direction('SOUTH').inverse(inverseEnabled);
        }
    }

    this.inverse = function (mustInverse) {
        if (!mustInverse) {
            return this;
        }
        switch (this.dir) {
            case 'SOUTH':
                return new Direction('NORTH');
            case 'EAST':
                return new Direction('WEST');
            case 'NORTH':
                return new Direction('SOUTH');
            case 'WEST':
                return new Direction('EAST');
        }
    }

    this.copy = function () {
        return new Direction(this.dir);
    }

    this.toString = function () {
        return this.dir;
    }

    this.equals = function (otherDir) {
        return this.dir == otherDir.dir;
    }
}

function debug(value) {
    printErr(JSON.stringify(value));
}