%% entrypoint of face detection function
function main
clc %clear command window
close all %close all the windows

% create face detection object
faceDetector = vision.CascadeObjectDetector;
mainPart(faceDetector);%main part of the face dectection function
end

%% main part function
function mainPart(faceDetector)
%call the FaceDetection function and return the image, coordinates and a boolean value
%coordinates preserve the coordinates of all the bounding boxes
[im, coordinates, I] = FaceDetection(faceDetector);
if isempty(I) %judge if or not select a picture, if not close the program
   return;
end

imA = elderzucker_tim(im); %use the Elder-Zucker algorithm to get depth image of all image
imB = huhaan_tim(im);%use Hu-Haan algorithm to get depth information of all image
%preserve coordinates as coordinateA and coordinateB to use two algorithms
coordinateA = coordinates;
coordinateB = coordinates;
%row is the number of coordinates
[row, ] = size(coordinates);
%we choose the depth of the middle point of the bounding box as the face's depth
for i=1:row
    coordinateA(i,3) = imA(coordinateA(i,2)+int16(coordinateA(i,4)/2),coordinateA(i,1)+int16(coordinateA(i,4)/2));
    coordinateB(i,3) = imB(coordinateB(i,2)+int16(coordinateA(i,4)/2),coordinateB(i,1)+int16(coordinateA(i,4)/2));
end

%normalize these two coordinates to range [0,1]
coordinateA = normArray(coordinateA);
coordinateB = normArray(coordinateB);
%calculate the mean value of these two algorithms
for i=1:row
   coordinates(i,3) = (coordinateA(i,3)+coordinateB(i,3))/2.0;
end
%save the coordinates
savePicture(coordinates);
end

%% normalize coordinate function
function coordinates = normArray(coordinate)
[row,] = size(coordinate);
maxCoor = max(coordinate(:,3));
minCoor = min(coordinate(:,3));
mip = maxCoor - minCoor;
for i=1:row
    coordinate(i,3) = (coordinate(i,3) - minCoor)/mip;
end
coordinates = coordinate;
end


%% preserve file function
function I = savePicture(coordinates)
[row,] = size(coordinates);
%for i=1:row
%    coordinates(i,3) = mrsmap2(coordinates(i,2),coordinates(i,1));
%end

I = 1;
resultID=fopen('result3D.txt','w');
fprintf(resultID,'Top-left coordinate is (0, 0, 0) \n');
fprintf(resultID,'Right direction is x axis \n');
fprintf(resultID,'Down direction is y axis \n');
fprintf(resultID,'Direction vertical to image and point to you is z axis \n');
fprintf(resultID,'For example: z1 < z2 , face z1 behind face z2 \n');
fprintf(resultID,'------------------------------ \n');

for i=1:row
   fprintf(resultID,'Face %i: (%f, %f, %f) \n',i,coordinates(i,1),coordinates(i,2),coordinates(i,3));
end

fclose(resultID);
I = 0;
end

%% choose picture function
function I = SelectPicture()
[FileName,PathName] = uigetfile('*.jpg', 'Please select a picture!');
if isequal(FileName,0)
    disp('No choosed picture, please reselect!')
    I = [];
else
    I = imread(fullfile(PathName,FileName));
end
end

%% call the GetFaces function
function [I_faces, bbox] = GetFaces(faceDetector, I)
% get information of bounding boxes
bbox = step(faceDetector, I);
%according to RGB image or gray image to choose different color of bounding boxes
if size(I, 3) == 1
    if mean(I(:)) > 128
        shapeInserter = vision.ShapeInserter();
    else
        shapeInserter = vision.ShapeInserter('BorderColor','White');
    end
else
    shapeInserter = vision.ShapeInserter('BorderColor','Custom','CustomBorderColor',[255 0 0]);%red color of bounding box
end

I_faces = step(shapeInserter, I, int32(bbox));
end

%FaceDetection function
function [im, coordinates, I] = FaceDetection(faceDetector)
    function BtnDownFcn(h, evt)%mouse click event
        mainPart(faceDetector);
    end

I = SelectPicture();
if isempty(I)
    return
end

[I_faces, bbox] = GetFaces(faceDetector, I);

close all
fig1 = figure;
pos1 = get(fig1,'Position');
set(fig1,'Position',[10 pos1(2:4)]);
set(fig1,'WindowButtonDownFcn',@BtnDownFcn);
%show figure
figure(fig1)
imshow(I_faces)
title('Click on the picture to choose another picture!')
%strText=[];
numCoordinates = size(bbox, 1);
% preserve the coordinates of array
coordinates = zeros(numCoordinates,4);
%write file of 2d coordinates
fileID=fopen('result2D.txt','w');
fprintf(fileID,'Top-left coordinate is (0, 0) \n');
fprintf(fileID,'Right direction is x axis \n');
fprintf(fileID,'Down direction is y axis \n');
fprintf(fileID,'------------------------------\n');
for i = 1:size(bbox, 1)
    fprintf(fileID,'Face %i: (%f,%f)\n',i,bbox(i,1),bbox(i,2));
    coordinates(i,:) = [bbox(i,1), bbox(i,2), 0, bbox(i, 4)];
    text(bbox(i, 1), bbox(i, 2), mat2str(i), 'color', 'r')
end
fclose(fileID);

im = I;%return the original image to transfer to two algorithms
end
