//
//  CamViewController.h
//  TagTheBus
//
//  Created by Pascal Viguié on 07/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import <UIKit/UIKit.h>


#define CAM_WIDTH               640
#define CAM_HEIGHT              480

@import AVFoundation;

@interface CamViewController : UIViewController <AVCaptureVideoDataOutputSampleBufferDelegate, UIAlertViewDelegate>

@property int stationIdx;
@property (weak, nonatomic) IBOutlet UIView *camView;
@property (weak, nonatomic) IBOutlet UIButton *btnShoot;
- (IBAction)OnPhotoPick:(id)sender;

+(NSString*)getPicFile:(NSDate*)withDate andType:(BOOL)thumbnail;

@end
