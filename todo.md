# Roadmap

http://sorea.profitux.cz/patching/#anim_mul

## MUL Module

Tools for reading UOs MUL files.

- [x] reading index files
- [x] reading art.mul
- [x] reading animX.mul
- [ ] facade over multiple animX.mul files
- [ ] animdata.mul - this file has mostly zeros is it any good?
- [ ] cliloc files
- [ ] fonts.mul (do I want to use original fonts?)
- [x] gumpart.mul
- [ ] hues.mul
- [ ] mapX.mul
- [ ] facade over mapX.mul files
- [ ] multi.mul
- [ ] skills.mul
- [ ] sound.mul
- [ ] speech.mul
- [ ] stadifX.mul + facade
- [ ] staticsX.mul + facade
- [ ] tiledata.mul
- [ ] unifontX.mul + facade (co to za plik?)

## Data Module?

I'd like to have some kind of abstraction over UO data so I can store data in other 
format if I'd like to recode MUL files into some more efficient structure.

## Network Module

UO Network protocol implementation

## Graphics Module

- [ ] choose graphics library (slick2d, sdl, raw lwjgl?, libgdx, maybe vulkan? :o)
- [ ] displaying images
- [ ] playing animations

## Sound Module

- [ ] playing sounds
- [ ] loading sounds

## Game 

- [ ] map rendering 
- [ ] entities
- [ ] player + controls
- [ ] GUI

