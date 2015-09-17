var N = parseInt(readline());
var C = parseInt(readline());
var giftPrice = C;
debug(giftPrice);

var participants = [];
for (var i = 0; i < N; i++) {
    var B = parseInt(readline());
    participants.push({id: i, contributionMax: B, contribution: 0});
}
//debug(participants);

if (giftPrice > participants.reduce(function (somme, participant) {
        return somme + participant.contributionMax;
    }, 0)) {
    print('IMPOSSIBLE');
    exit;
}

participantsByContributionMax = participants
    .reduce(function (newArray, participant) { // Copy array
        newArray.push(participant);
        return newArray;
    }, [])
    .sort(function (partA, partB) { // Sort by smallest contributionMax
        return partA.contributionMax > partB.contributionMax;
    });

var resteARepartir = giftPrice;
totalContribution = 0;
var step = 0;
while (resteARepartir > 0) {
    step++;
    //debug(participantsByContributionMax, step);
    var contribMaxRestanteParticipant1 = participantsByContributionMax[0].contributionMax - participantsByContributionMax[0].contribution;
    if ((contribMaxRestanteParticipant1 * participantsByContributionMax.length) > resteARepartir) {
        debug('Répartition par division', step);
        debug('ContribMaxRestanteParticipant1' + contribMaxRestanteParticipant1, step);
        debug('ResteARepartir ' + resteARepartir, step);
        debug('NB paricipants restants ' + participantsByContributionMax.length, step);

        // On divise le total et on le répartit sur les participants
        var aRepartir = Math.floor(resteARepartir / participantsByContributionMax.length);
        debug("ARepartir" + aRepartir, step);
        debug(participantsByContributionMax.length, step);
        var reste = resteARepartir % participantsByContributionMax.length;
        for (var i = 0; i < participantsByContributionMax.length; i++) {
            participantsByContributionMax[i].contribution += aRepartir;
            //if (i == participantsByContributionMax.length-1){
            // C'est le dernier on lui file le reste
            //    participantsByContributionMax[i].contribution += reste;
            //}
        }
        if (reste > 0) {
            debug('test' + reste);
            for (var i = (participantsByContributionMax.length - 1); reste > 0; i--) {
                debug('i' + i);
                participantsByContributionMax[i].contribution += 1;
                reste -= 1;
            }
        }
        resteARepartir = 0;
    } else {
        debug('Répartition par max', step);
        debug("step " + step + " " + participantsByContributionMax.length + "*" + contribMaxRestanteParticipant1 + "=" + participantsByContributionMax.length * contribMaxRestanteParticipant1 + ' ' + resteARepartir, step)
        // On repartit le max de participant 1 sur tous les participants et on recommence
        participantsByContributionMax.forEach(function (participant) {
            participant.contribution += contribMaxRestanteParticipant1;
            totalContribution += contribMaxRestanteParticipant1;
            resteARepartir -= contribMaxRestanteParticipant1;
        });
        // On supprime tous les participants qui ont atteint leur limite
        participantsByContributionMax = participantsByContributionMax.reduce(function (newArr, participant) {
            //debug(participant.contribution + " " + participant.contributionMax,step);

            if (participant.contribution == participant.contributionMax) {
                //debug("                degage",step);
                return newArr;
            }
            newArr.push(participant);
            return newArr;
        }, []);
        debug("step " + step + " " + participantsByContributionMax.length + " " + totalContribution + ' ' + resteARepartir, step)
    }
}
debug(step);

// On affiche la contribution de chaque participant
step = 0;
participants.sort(function (partA, partB) {
    return partA.contribution > partB.contribution;
}).forEach(function (participant) {
    print(participant.contribution);
    //printDebug(participant.contribution,step++);
    //debug(participant.contribution,step++);
});

function debug(value, step) {
    if (step != null && step != 71) {
        return;
    }
    printErr(JSON.stringify(value));
}

function printDebug(value, step) {
    if (step < 600 || step >= 900) {
        return;
    }
    print(value);
}