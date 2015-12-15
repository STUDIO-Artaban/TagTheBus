//
//  CamViewController.m
//  TagTheBus
//
//  Created by Pascal Viguié on 07/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import "CamViewController.h"
#import "ListViewController.h"

#import "StationData.h"
#import "AlbumData.h"

#import "AlbumViewController.h"


#define BGRA_BUFFER_SIZE        (CAM_WIDTH * CAM_HEIGHT * 4)
#define RGB_BUFFER_SIZE         (CAM_WIDTH * CAM_HEIGHT * 3) // Picture

#define THM_WIDTH               (CAM_WIDTH / 8)
#define THM_HEIGHT              (CAM_HEIGHT / 8)
#define THM_BUFFER_SIZE         (THM_WIDTH * THM_HEIGHT * 3) // Thumbnail (80*60)

@implementation CamViewController {
    
    int stationID;
    
    AVCaptureSession* session;
    BOOL shoot;
    char* imgPick;
}

@synthesize stationIdx;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    stationID = [[(NSDictionary*)[ListViewController getStations][stationIdx] valueForKey:STATION_ID] intValue];
    self.title = [NSString stringWithFormat:@"%d - %@", stationID,
                  [(NSDictionary*)[ListViewController getStations][stationIdx] valueForKey:STATION_STREET],
                  nil];
    
    shoot = FALSE;

    // Initialize camera
    session = [[AVCaptureSession alloc] init];
    session.sessionPreset = AVCaptureSessionPreset640x480;
    
    CALayer* viewLayer = self.camView.layer;
    NSLog(@"viewLayer = %@", viewLayer);
    
    AVCaptureVideoPreviewLayer* captureVideoPreviewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:session];
    
    [self.camView setNeedsLayout];
    [self.camView layoutIfNeeded];
    
    captureVideoPreviewLayer.frame = self.camView.bounds;
    captureVideoPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    captureVideoPreviewLayer.connection.videoOrientation = AVCaptureVideoOrientationPortrait;
    [self.camView.layer addSublayer:captureVideoPreviewLayer];
    
    AVCaptureDevice* device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    
    NSError* error = nil;
    AVCaptureDeviceInput* input = [AVCaptureDeviceInput deviceInputWithDevice:device error:&error];
    if (!input)
        NSLog(@"ERROR: Failed to open camera: %@", error);
    
    [session addInput:input];
    
    // Prepare camera image picker
    AVCaptureVideoDataOutput* videoOutput = [[AVCaptureVideoDataOutput alloc] init];
    NSArray* formatArray = [videoOutput availableVideoCVPixelFormatTypes];
    BOOL bgraFormat = NO;
    for (NSNumber* format in formatArray)
        if ([format integerValue] == kCVPixelFormatType_32BGRA)
            bgraFormat = YES;
    
    if (bgraFormat == NO) {
        
        NSLog(@"ERROR: 'kCVPixelFormatType_32BGRA' video format not supported (line:%d)", __LINE__);
        [self.btnShoot setEnabled:FALSE];
    }
    else {

        [videoOutput setAlwaysDiscardsLateVideoFrames:YES];
        [videoOutput setVideoSettings:[NSDictionary dictionaryWithObject:[NSNumber
                numberWithInt:kCVPixelFormatType_32BGRA] forKey:(id)kCVPixelBufferPixelFormatTypeKey]];
        dispatch_queue_t camQueue = dispatch_queue_create("camQueue", NULL);
        [videoOutput setSampleBufferDelegate:self queue:camQueue];
        if ([session canAddOutput:videoOutput])
            [session addOutput:videoOutput];
        else
            NSLog(@"ERROR: Failed to add video output (line:%d)", __LINE__);
    }
    [session startRunning];
}
- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [session stopRunning];
    free(imgPick);
}
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    imgPick = malloc(BGRA_BUFFER_SIZE);
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

+(NSString*)getPicFile:(NSDate*)withDate andType:(BOOL)thumbnail {

    NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString* documents = [paths objectAtIndex:0];
    NSDateFormatter* dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat: @"dd-MM-yyyy_HH:mm:ss"];
    
    return [documents stringByAppendingPathComponent:[NSString stringWithFormat:(thumbnail)? @"%@.png":@"%@_THMB.png",
                                                      [dateFormat stringFromDate:withDate], nil]];
}

//
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {

    if (buttonIndex == 1) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{

            // Create & Save PNG photo and thumbnail into documents directory
            NSDate* date = [[NSDate alloc] init];
            NSString* imgPath = [CamViewController getPicFile:date andType:FALSE];
            NSString* thmPath = [CamViewController getPicFile:date andType:TRUE];

            UInt8* picBuffer = malloc(RGB_BUFFER_SIZE);
            for (unsigned int i = 0, j = 0; i < BGRA_BUFFER_SIZE; i += 4, j += 3) {

                picBuffer[j + 2] = imgPick[i + 0]; // B
                picBuffer[j + 1] = imgPick[i + 1]; // G
                picBuffer[j + 0] = imgPick[i + 2]; // R
            }
            UInt8* thmBuffer = malloc(THM_BUFFER_SIZE);
            for (unsigned int y = 0, i = 0; y < CAM_HEIGHT; ++y) {
                for (short x = 0; x < CAM_WIDTH; ++x) {

                    if ((!(y % 8)) && (!(x % 8))) {
                        int j = (y * CAM_WIDTH * 4) + (x * 4);

                        thmBuffer[i + 2] = imgPick[j + 0]; // B
                        thmBuffer[i + 1] = imgPick[j + 1]; // G
                        thmBuffer[i + 0] = imgPick[j + 2]; // R
                        
                        i += 3;
                    }
                }
            }
            CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
            CFDataRef picData = CFDataCreate(NULL, picBuffer, RGB_BUFFER_SIZE);
            CFDataRef thmData = CFDataCreate(NULL, thmBuffer, THM_BUFFER_SIZE);
            
            CGDataProviderRef picProvider = CGDataProviderCreateWithCFData(picData);
            CGDataProviderRef thmProvider = CGDataProviderCreateWithCFData(thmData);
            
            CGImageRef picImageRef = CGImageCreate(CAM_WIDTH, CAM_HEIGHT, 8, 24, CAM_WIDTH * 3, colorSpace,
                                        kCGBitmapByteOrderDefault, picProvider, NULL, true, kCGRenderingIntentDefault);
            CGImageRef thmImageRef = CGImageCreate(THM_WIDTH, THM_HEIGHT, 8, 24, THM_WIDTH * 3, colorSpace,
                                        kCGBitmapByteOrderDefault, thmProvider, NULL, true, kCGRenderingIntentDefault);

            CFRelease(picData);
            CFRelease(thmData);
            CGDataProviderRelease(picProvider);
            CGDataProviderRelease(thmProvider);
            CGColorSpaceRelease(colorSpace);

            UIImage* picImage = [[UIImage alloc] initWithCGImage:picImageRef];
            UIImage* thmImage = [[UIImage alloc] initWithCGImage:thmImageRef];
            //UIImageWriteToSavedPhotosAlbum(picImage, nil, nil, nil);
            //UIImageWriteToSavedPhotosAlbum(thmImage, nil, nil, nil);
            
            NSData* dataPic =  UIImagePNGRepresentation(picImage);
            NSData* dataThumb =  UIImagePNGRepresentation(thmImage);
            if (([dataPic writeToFile:imgPath atomically:NO]) && ([dataThumb writeToFile:thmPath atomically:NO])) {
            
                // Add into DB
                NSString* title = [[alertView textFieldAtIndex:0] text];
                if (title.length == 0)
                    title = @"<NON DEFINI>";

                NSDictionary* newPhoto = [[NSDictionary alloc] initWithObjectsAndKeys:
                                          title, PHOTO_TITLE,
                                          date, PHOTO_DATE, nil];
                [[ListViewController getDB] add:newPhoto toAlbum:stationID];
            }
            else
                NSLog(@"ERROR: Failed to save photo!");

            CGImageRelease(picImageRef);
            CGImageRelease(thmImageRef);
            free(picBuffer);
            free(thmBuffer);

            // Back to the photos list
            [[NSOperationQueue mainQueue] addOperationWithBlock:^{
                [self.navigationController popViewControllerAnimated:YES];
            }];
        });
    }
}

//
-(void)captureOutput:(AVCaptureOutput *)captureOutput didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer
      fromConnection:(AVCaptureConnection *)connection {

    CVImageBufferRef camBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    CVPixelBufferLockBaseAddress(camBuffer, 0);
    if (shoot) {

        shoot = FALSE;
        memcpy(imgPick, (uint8_t*)CVPixelBufferGetBaseAddress(camBuffer), BGRA_BUFFER_SIZE);
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            
            UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"TagTheBus" message:@"Entrez le titre de la photo:"
                        delegate:self cancelButtonTitle:@"Annuler" otherButtonTitles:@"Valider", nil];
            alert.alertViewStyle = UIAlertViewStylePlainTextInput;
            [alert show];
        }];
    }
    CVPixelBufferUnlockBaseAddress(camBuffer, 0);
}

//
- (BOOL)shouldAutorotate { return FALSE; }
- (IBAction)OnPhotoPick:(id)sender { shoot = TRUE; }

/*
 #pragma mark - Navigation
 
 // In a storyboard-based application, you will often want to do a little preparation before navigation
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
 // Get the new view controller using [segue destinationViewController].
 // Pass the selected object to the new view controller.
 }
 */

@end
