@echo ON

SET targetFileNameUrl=%1
SET compositionImagesUrl=%2
SET rectangleCount=%3
SET collageOutputFileUrl=%4

SET seedFactor = 5
SET populationCount = 1000
SET mutationRate = 0.6
SET crossOverRate = 0.01
SET subSampling = 15
SET pixelDistance = 50

java -jar wisemen-2.0-SNAPSHOT.jar %targetFileNameUrl% %compositionImagesUrl% %rectangleCount% %collageOutputFileUrl% %seedFactor% %populationCount% %mutationRate% %crossOverRate% %subSampling% %pixelDistance%