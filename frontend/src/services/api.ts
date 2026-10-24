// import { ApiBridge } from '../bridge/api_bridge';
import type { Track } from '../util/music';

// const topEndpoint = '/me/top/';

export async function get(endpoint: string) {
    console.log(endpoint);
}

export async function getTopTracks(): Promise<Track[]> {
    const rows: Track[] = [
        { name: 'Mycelium', artists: [{ name: 'King Gizzard & The Lizard Wizard' }], album: "Ice, Death, Planets, Lungs, Mushrooms And Lava" },
        {
            name: 'So What',
            artists: [
                { name: 'Miles Davis' },
                { name: 'John Coltrane' },
                { name: 'Cannonball Adderley' },
                { name: 'Bill Evans' },
                { name: 'Paul Chambers' },
                { name: 'Jimmy Cobb' },
            ],
            album: 'Kind of Blue',
        },
        { name: 'My childhood was a period of waiting for the moment when I could send everyone and everything connected with it to hell', artists: [{ name: 'Ciśnienie' }], album: '(angry noises)' },
        { name: 'Dreams', artists: [{ name: 'Fleetwood Mac' }], album: 'Rumours' },
        { name: 'Clocks', artists: [{ name: 'Coldplay' }], album: 'A Rush of Blood to the Head' },
        { name: 'Ocean Eyes', artists: [{ name: 'Billie Eilish' }], album: 'Dont Smile at Me' },
        { name: 'Blinding Lights', artists: [{ name: 'The Weeknd' }], album: 'After Hours' },
        { name: 'Bad Guy', artists: [{ name: 'Billie Eilish' }], album: 'When We All Fall Asleep, Where Do We Go?' },
        { name: 'Take On Me', artists: [{ name: 'a-ha' }], album: 'Hunting High and Low' },
        { name: 'Mr. Brightside', artists: [{ name: 'The Killers' }], album: 'Hot Fuss' },
        { name: 'Royals', artists: [{ name: 'Lorde' }], album: 'Pure Heroine' },
        { name: 'Get Lucky', artists: [{ name: 'Daft Punk' }, { name: 'Pharrell Williams' }], album: 'Random Access Memories' },
        { name: 'Levitating', artists: [{ name: 'Dua Lipa' }], album: 'Future Nostalgia' },
        { name: 'Seven Nation Army', artists: [{ name: 'The White Stripes' }], album: 'Elephant' },
        { name: 'Feel Good Inc.', artists: [{ name: 'Gorillaz' }], album: 'Demon Days' },
        { name: 'Paper Planes', artists: [{ name: 'M.I.A.' }], album: 'Kala' },
    ];
    return rows;

    // ApiBridge.get(topEndpoint + 'tracks');
}
