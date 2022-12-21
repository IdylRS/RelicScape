const fs = require('fs');
const prompt = require('prompt-sync')({ sigint: true });

const fileLocation = `./src/main/resources/com/relicscape/bounties.json`;
const npcIdLocation = `./npcIDs.json`;

const npcIDs = JSON.parse(fs.readFileSync(npcIdLocation));

let data = {
    id: -1,
    npcName: "",
    npcIDs: [],
    tier: 1,
    minCombatLevel: 3
}

let newData = JSON.parse(fs.readFileSync(fileLocation));

const npcName = prompt("What is the name of the npc? ");
const npcKey = npcName
    .toUpperCase()
    .replaceAll(" ", "_")
    .replaceAll("'", "")
    .trim();

let ids = Object.keys(npcIDs).map(npc => {
    if (npc.includes(npcKey)) {
        if (npc.includes("HARD")) return -1;
        console.log(`Found ${npc}`)
        return npcIDs[npc];
    }
    return -1
}).filter(id => id > 0);

data.npcName = npcName;
data.npcIDs.push(...ids);

data.minCombatLevel = +prompt("What is the minimum combat level to receive the bounty? ");
data.tier = +prompt("What is the tier of the bounty? ");
data.id = newData.length;

newData.push(data);

fs.writeFileSync(fileLocation, JSON.stringify(newData));