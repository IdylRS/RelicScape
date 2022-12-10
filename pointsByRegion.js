const fs = require('fs');
const prompt = require('prompt-sync')({sigint: true});

const fileLocation = `./src/main/resources/com/rslocked/tasks.json`;

const data = JSON.parse(fs.readFileSync(fileLocation));

let regions = {};

function getValue(tier) {
    switch(tier) {
        case 1:
            return 10;
        case 2:
            return 25;
        case 3:
            return 50;
        case 4:
            return 100;
        case 5:
            return 250;
        case 6:
            return 500;
    }
}

data.map(task => {
    if(regions[task.region]) {
        regions[task.region] += getValue(task.tier);
    }
    else {
        regions[task.region] = getValue(task.tier)
    }
});

Object.keys(regions).map(region => {
    console.log(`${region}: ${regions[region]} points`)
})