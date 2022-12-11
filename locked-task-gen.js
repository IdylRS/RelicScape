const fs = require('fs');
const prompt = require('prompt-sync')({ sigint: true });

const fileLocation = `./src/main/resources/com/relicscape/tasks.json`;

let data = {
    id: "",
    tier: -1,
    type: "",
    description: "",
    region: "",
    completionIDs: [],
    locations: [],
    gainedXP: -1,
    useRegionID: false
}

let askLoc = true;
let askRegion = true;
let askCompID = true;
let itemName = "";
let itemKey = '';
let npcName = "";
let npcKey = "";

let prefix = '';
let suffix = '';

let newData = JSON.parse(fs.readFileSync(fileLocation));

const itemIDs = JSON.parse(fs.readFileSync('./itemIDs.json'));
const npcIDs = JSON.parse(fs.readFileSync('./npcIDs.json'));


const strongholdSlayerCave = [
    {
        x: 2771,
        y: 10011,
        plane: 0
    },
    {
        x: 2714,
        y: 10008,
        plane: 0
    },
    {
        x: 2704,
        y: 9962,
        plane: 0
    }
]

data.type = getType(prompt('What type of task? [en, eq, c, k, l, m]'));
data.tier = +prompt('What tier is the task?');
console.log(data.type)
if (data.type == 'QUEST') fillQuestData();
else fillData();

function fillData() {
    if (data.type == "LOOT" || data.type == 'EQUIP' || data.type === 'CREATE') {
        itemName = prompt('What is the item to be obtained?');
        itemKey = itemName
            .toUpperCase()
            .replaceAll(" ", "_")
            .replaceAll("-", "")
            .replaceAll("(", "")
            .replaceAll(")", "")
            .replaceAll("'", "")
            .trim();
        let itemID = itemIDs[itemKey];

        console.log(itemKey)

        if (itemID) {
            data.completionIDs.push(itemID);
            console.log(`Found item id for ${itemName}: ${itemID}`)
        }
        else {
            console.log('Could not find item id, please enter manually later.');
        }
    }

    if (data.type == "KILL") {
        npcName = prompt('What is the npc to be killed?');
        console.log(npcName)
        npcKey = npcName
            .toUpperCase()
            .replaceAll(" ", "_")
            .replaceAll("'", "")
            .trim();
        let ids = Object.keys(npcIDs).map(npc => {
            if (npc.startsWith(npcKey)) {
                if (npc.includes("HARD")) return -1;
                console.log(`Found ${npc}`)
                return npcIDs[npc];
            }
            return -1
        }).filter(id => id > 0);

        if (ids.length > 0) {
            data.completionIDs.push(...ids);
            console.log(`Found npc ids for ${npcName}: ${ids}`)
        }
        else {
            console.log('Could not find npc id, please enter manually later.');
        }
    }

    if ((data.type === 'KILL' && npcName) || (data.type == 'EQUIP' && itemName)) {
        prefix = prompt('What is the description prefix: ');
        suffix = prompt('What is the description suffix: ');
    }
    else {
        data.description = data.type == 'LOOT' && itemName ? prompt('What is the description prefix: ') : prompt('Describe the task:');
    }


    if (data.type == 'LOOT' && itemName) {
        data.description += ` ${itemName}`
    }

    if ((data.type === 'KILL' && npcName) || (data.type == 'EQUIP' && itemName)) {
        data.description = `${prefix} ${npcName || itemName}${suffix ? ` ${suffix}` : ''}`
    }

    while (askRegion) {
        data.region = getRegion(prompt('What region does this task take place in?'));
        if (data.region) {
            data.region = data.region.toUpperCase();
            askRegion = false;
        }
    }

    while (askLoc) {
        askLoc = addLocation(prompt('What is the x y plane?'));
    }

    data.useRegionID = prompt('Use only the location Region ID?') ? true : false;

    while (askCompID) {
        askCompID = addCompID(prompt('What is the completion ID for this task?'));
    }

    data.gainedXP = +prompt("What is the xp target of this task?") || -1;

    data.skill = getSkill(prompt("What skill? "));
}

function fillQuestData() {
    let questName = prompt('What is the quest name? ');
    data.quest = questName.toUpperCase().replaceAll(' ', '_').replaceAll("'", "").replaceAll('!', '').replaceAll('-', ' ').trim();
    data.description = `Complete ${questName.startsWith("The") ? '' : 'the '}${questName}${questName.endsWith("Quest") ? '' : ' quest'}`

    while (askRegion) {
        data.region = getRegion(prompt('What region does this task take place in?'));
        if (data.region) {
            data.region = data.region.toUpperCase();
            askRegion = false;
        }
    }
}

data.id = Math.floor(Math.random() * 99999)

newData.push(data);

fs.writeFileSync(fileLocation, JSON.stringify(newData));

function addLocation(coords) {
    if (!coords) return false;

    if (coords === 'ssc') {
        data.locations.push(...strongholdSlayerCave)
        return true;
    }

    const coordsSplit = coords.split(' ');

    data.locations.push({ x: coordsSplit[0], y: coordsSplit[1], plane: coordsSplit[2] })
    return true;
}

function addCompID(id) {
    if (!id) return false;

    data.completionIDs.push(+id);
    return true;
}

function getType(type) {
    switch (type) {
        case 'en':
            return 'ENTER';
        case 'eq':
            return 'EQUIP';
        case 'c':
            return `CREATE`;
        case 'k':
            return 'KILL';
        case 'l':
            return 'LOOT';
        case 'm':
            return 'MAGIC';
        case 'q':
            return 'QUEST';
        case 'lvl':
            return 'LEVEL';
        default:
            return '';
    }
}

function getRegion(region) {
    switch (region) {
        case 'asg':
            return 'Asgarnia';
        case 'mis':
            return 'Misthalin';
        case 'mor':
            return 'Morytania';
        case 'wil':
            return 'Wilderness';
        case 'kan':
            return 'Kandarin';
        case 'kar':
            return 'Karamja';
        case 'tir':
            return 'Tirannwn';
        case 'uz':
            return 'Upper Zeah';
        case 'lz':
            return 'Lower Zeah';
        case 'keb':
            return 'Kebos';
        case 'des':
            return 'Desert';
        case 'fre':
            return 'Fremennik';
        case 'all':
            return 'ALL';
        default:
            return false;
    }
}

function getSkill(skill) {
    return skill.toUpperCase();
}
