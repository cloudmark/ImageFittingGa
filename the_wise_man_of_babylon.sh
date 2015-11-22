#!/bin/sh
if [ "$#" -ne 4 ]; then
  echo "Usage: $0 <target> <composition_image_url> <rectangleCount> <collageOutputFileUrl>" >&2
  exit 1
fi


TargetFileNameUrl=$1
CompositionImagesUrl=$2
RectangleCount=$3
CollageOutputFileUrl=$4

SeedFactor=5
PopulationCount=1000
MutationRate=0.6
CrossOverRate=0.01
SubSampling=15
PixelDistance=50

echo "Target: $TargetFileNameUrl"
echo "CompositionImagesUrl: $CompositionImagesUrl"
echo "RectangleCount: $RectangleCount"
echo "CollageOutputFileUrl: $CollageOutputFileUrl"
echo "SeedFactor: $SeedFactor"
echo "PopulationCount: $PopulationCount"
echo "MutationRate: $MutationRate"
echo "CrossOverRate: $CrossOverRate"
echo "SubSampling: $SubSampling"
echo "PixelDistance: $PixelDistance"
java -Xmx3072m -Xms512m -jar "target/wisemen-2.0-SNAPSHOT.jar" "$TargetFileNameUrl" "$CompositionImagesUrl" "$RectangleCount" "$CollageOutputFileUrl" "$SeedFactor" "$PopulationCount" "$MutationRate" "$CrossOverRate" "$SubSampling" "$PixelDistance"
