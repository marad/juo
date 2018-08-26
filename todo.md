# Roadmap

## Resources

- http://sorea.profitux.cz/patching/#anim_mul
- https://github.com/msturgill/ultimasdk/blob/master/Ultima
- http://www.runuo.net/forum/forum/runuo-art-amd-maps/artwork/1867-introduction-to-mul-files-tutorial

## MUL Module

Tools for reading UOs MUL files.

- [x] reading index files
- [x] reading art.mul
- [x] reading animX.mul
- [x] bodyconv.def
- [x] facade over multiple animX.mul files
- [ ] animdata.mul - this file has mostly zeros is it any good?
- [ ] cliloc files
- [ ] fonts.mul (do I want to use original fonts?)
- [x] gumpart.mul
- [x] hues.mul
- [ ] mapX.mul
- [ ] facade over mapX.mul files
- [ ] multi.mul
- [ ] skills.mul (skills.idx)
- [ ] sound.mul
- [ ] speech.mul
- [ ] stadifX.mul + facade
- [ ] staticsX.mul + facade
- [ ] tiledata.mul
- [ ] unifontX.mul + facade (co to za plik?)
- [ ] light.mul (lightidx.mul)
- [ ] multimap.rle - ??
- [ ] tex.idx - ??
- [ ] radarcol.mul - ??
- [ ] texmaps.mul - ??
- [ ] speach.mul - ??
- [ ] langcode.iff - ??
## Data Module?

I'd like to have some kind of abstraction over UO data so I can store data in other 
format if I'd like to recode MUL files into some more efficient structure.

## Network Module

UO Network protocol implementation

## Graphics Module

- [x] choose graphics library - libgdx
- [x] converting images to textures
- [ ] playing animations

## Sound Module

- [ ] playing sounds
- [ ] loading sounds

## Game 

- [ ] map rendering 
- [ ] entities
- [ ] player + controls
- [ ] GUI

