# Locomotion -> 26.3 (tahap 1, BELUM dikompilasi)

Sudah: build.gradle (Loom 1.18.2, tanpa mappings, Java 25), gradle 9.7.1, loader 0.19.5, Fabric API 0.161.0+26.3,
YACL 3.9.6+26.3, ModMenu 21.0.0-beta.1, fabric.mod.json, workflow Java 25, rename Identifier/AvatarRenderState/AvatarRenderer/PlayerModelType.

Belum / perlu lewat error build:
- LocomotionResources + LocomotionFabric: reload listener Fabric Resource Loader v1 (cek signature setelah build gagal).
- FirstPersonPlayerRenderer + MixinItemInHandRenderer: masih MultiBufferSource (1.21.4), harus ke SubmitNodeCollector.
- LocomotionMultiVersionWrappers: getSpearUseAnimation sekarang harus valid (ItemUseAnimation.SPEAR).
- Input SDL3 (26.3) tidak dipakai mod ini, tapi mixin render/GameRenderer mungkin berubah.
Lisensi upstream: dilarang publish port ke CurseForge/Modrinth tanpa izin penulis; pakai pribadi saja.

## Tahap 2 (setelah log build #1)
- Perbaikan mekanis: Tuple, BlockEntityType.getKey, EntityTypes.*, Inventory.getSelectedSlot, gui.screen(), FileToIdConverter, ResourceLoader v1.
- Renderer tangan/item dinonaktifkan sementara (MixinItemInHandRenderer, MixinItemTransform, FirstPersonPlayerRenderer lama ada di folder disabled-26.3/).
- TODO: player.swingTime tidak ada di 26.3 (diganti 0), SwingSource.CLIENT dicek via nama enum.

## Tahap 3 (renderer tangan diport)
- Penyebab "tangan tetap vanila": 3 file renderer dimatikan di tahap 2 (sudah diaktifkan lagi di sini) + mixin kamera gagal
  (`bobHurt` sekarang `(CameraRenderState, PoseStack)`).
- FirstPersonPlayerRenderer sekarang memakai SubmitNodeCollector (submitModelPart / ItemStackRenderState.submit / submitCustomGeometry).
- Kamera dipecah jadi MixinGameRenderer (renderLevel), MixinGameRendererCamera (bobHurt), MixinGameRendererBob (bobView)
  supaya satu signature salah tidak mematikan yang lain. Cek log: "Mixin apply for mod locomotion failed".
- BELUM DIKOMPILASI. Tebakan API yang paling mungkin meleset: RenderTypes (paket rendertype), skin.body().texturePath(),
  ItemModelResolver.updateForLiving, ItemTransform.apply, signature bobView.
