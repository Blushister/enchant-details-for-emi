# EMI Enchants — NeoForge 1.21.1

Addon EMI affichant les informations détaillées de chaque enchantement (niveaux,
items applicables, enchantements incompatibles, poids, coûts, table d'enchantement /
villageois / butin / trésor / malédiction, description).

## Origine & licence

Port **NeoForge 1.21.1** du mod [EMI Enchants](https://github.com/Mephodio/EmiEnchants)
de **Mephodio** (Fabric/Forge 1.20.1), sous licence **GPL-3.0-or-later**.
Conformément à la GPL, ce port reste sous GPL-3.0-or-later et son code source est
distribué (voir `src/`). Crédit original : Mephodio.

## Changements notables du port 1.21

La refonte des enchantements en 1.21 a imposé :
- lecture des enchantements via le **registre dynamique** (plus `BuiltInRegistries`) ;
- statuts via **tags** (`EnchantmentTags`) au lieu des méthodes booléennes supprimées ;
- **poids** (`getWeight()`) à la place de l'ancienne « rareté » supprimée ;
- items applicables via `getSupportedItems()` ;
- livres enchantés via le **composant de données** `STORED_ENCHANTMENTS`.

## Build

```
./gradlew build
```

Jar produit dans `build/libs/`.

## Dépendances

- NeoForge 1.21.1
- [EMI](https://modrinth.com/mod/emi) 1.1.x (requis, côté client)
