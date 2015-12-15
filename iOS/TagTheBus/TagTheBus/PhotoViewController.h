//
//  PhotoViewController.h
//  TagTheBus
//
//  Created by Pascal Viguié on 10/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MessageUI/MessageUI.h>


@interface PhotoViewController : UIViewController <MFMailComposeViewControllerDelegate>

@property int stationID;
@property int photoIdx;

@end
