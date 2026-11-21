# Locomotion -> 26.3 (tahap 1, BELUM dikompilasi)

Sudah: build.gradle (Loom 1.18.2, tanpa mappings, Java 25), gradle 9.7.1, loader 0.19.5, Fabric API 0.161.0+26.3,
YACL 3.9.6+26.3, ModMenu 21.0.0-beta.1, fabric.mod.json, workflow Java 25, rename Identifier/AvatarRenderState/AvatarRenderer/PlayerModelType.

Belum / perlu lewat error build:
- LocomotionResources + LocomotionFabric: reload listener Fabric Resource Loader v1 (cek signature setelah build gagal).
- FirstPersonPlayerRenderer + MixinItemInHandRenderer: masih MultiBufferSource (1.21.4), harus ke SubmitNodeCollector.
- LocomotionMultiVersionWrappers: getSpearUseAnimation sekarang harus valid (ItemUseAnimation.SPEAR).
- Input SDL3 (26.3) tidak dipakai mod ini, tapi mixin render/GameRenderer mungkin berubah.
Lisensi upstream: dilarang publish port ke CurseForge/Modrinth tanpa izin penulis; pakai pribadi saja.
