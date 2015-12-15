//
//  MapViewController.h
//  TagTheBus
//
//  Created by Pascal Viguié on 02/12/2015.
//  Copyright (c) 2015 STUDIO Artaban. All rights reserved.
//

#import <UIKit/UIKit.h>


@import GoogleMaps;

@interface MapViewController : UIViewController <CLLocationManagerDelegate, GMSMapViewDelegate>

@property (weak, nonatomic) IBOutlet UIView *mapView;
@property (weak, nonatomic) IBOutlet UIButton *selStation;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *itmGeoloc;

-(IBAction)Geolocation:(id)sender;

@end
