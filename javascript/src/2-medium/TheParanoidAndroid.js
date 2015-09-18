var inputs = readline().split(' ');
var nbFloors = parseInt(inputs[0]); // number of floors
var width = parseInt(inputs[1]); // width of the area
var nbRounds = parseInt(inputs[2]); // maximum number of rounds
var exitFloor = parseInt(inputs[3]); // floor on which the exit is found
var exitPos = parseInt(inputs[4]); // position of the exit on its floor
var nbTotalClones = parseInt(inputs[5]); // number of generated clones
var nbAdditionalElevators = parseInt(inputs[6]); // ignore (always zero)
var nbElevators = parseInt(inputs[7]); // number of elevators

var floorElevator = {};
for (var i = 0; i < nbElevators; i++) {
    var inputs = readline().split(' ');
    var elevatorFloor = parseInt(inputs[0]); // floor on which this elevator is found
    var elevatorPos = parseInt(inputs[1]); // position of the elevator on its floor
    floorElevator[elevatorFloor] = elevatorPos;
}

// game loop
while (true) {
    var inputs = readline().split(' ');
    var cloneFloor = parseInt(inputs[0]); // floor of the leading clone
    var clonePos = parseInt(inputs[1]); // position of the leading clone on its floor
    var direction = inputs[2]; // direction of the leading clone: LEFT or RIGHT

    var action;
    if (cloneFloor == exitFloor) {
        action = getActionToGoTo(clonePos, direction, exitPos);
    } else {
        action = getActionToGoTo(clonePos, direction, floorElevator[cloneFloor]);
    }


    // Write an action using print()
    // To debug: printErr('Debug messages...');

    print(action.getAction()); // action: WAIT or BLOCK
}

function getActionToGoTo(clonePos, direction, destPos) {
    printErr("getActionToGoTo(" + clonePos + "," + direction + "," + destPos + ")");
    var action = new Action("WAIT");
    switch (direction) {
        case "LEFT":
            if (clonePos < destPos) {
                action = new Action("BLOCK");
            }
            break;
        case "RIGHT":
            if (clonePos > destPos) {
                action = new Action("BLOCK");
            }
            break;
    }
    return action;
}

function Action(action) {
    this.action = action;

    printErr(this.action);

    this.getAction = function () {
        return this.action;
    }
}