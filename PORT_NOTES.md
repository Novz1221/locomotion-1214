# Locomotion -> Minecraft 1.21.4 (backport tidak resmi)

STATUS: BELUM DIKOMPILASI. Bangun lewat GitHub Actions, kirim log error untuk diperbaiki bertahap.

Dasar: Locomotion 0.1.12 (master) yang targetnya 1.21.11. Proyek disederhanakan jadi satu modul Fabric 1.21.4
(tanpa Stonecutter/NeoForge/Forge), mapping Mojang + Parchment.

## Fitur yang DIKERJAKAN
- Mesin animasi, resource (sequences/skeletons), config (YACL + ModMenu)
- Animasi first person (lengan + item + peta + kamera)

## Fitur yang DIHAPUS / BELUM
- Animasi chest/shulker (butuh sistem render 1.21.9+)
- Layar debug F3, spear/tombak, animasi blok tameng (BlocksAttacks 1.21.5+)
- Hook render third-person (MixinLivingEntityRenderer) -> tahap 2
- Posisi tali pancing, layer cape/elytra

## Perubahan API utama
Identifier->ResourceLocation, AvatarRenderState->PlayerRenderState, ItemUseAnimation->UseAnim,
SubmitNodeCollector->MultiBufferSource, AvatarRenderer->PlayerRenderer, PlayerModelType->PlayerSkin.Model,
resource reload -> SimpleSynchronousResourceReloadListener (Fabric API), mixin config defaultRequire=0 (mixin gagal tidak menutup game, cek log).
