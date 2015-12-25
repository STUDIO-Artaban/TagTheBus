//
//  MapViewController.m
//  TagTheBus
//
//  Created by Pascal Viguié on 02/12/2015.
//  Copyright (c) 2015 STUDIO Artaban. All rights reserved.
//

#import "MapViewController.h"
#import "ListViewController.h"

#import "StationData.h"
#import <CoreLocation/CoreLocation.h>
#import "AlbumViewController.h"


@interface MapViewController ()
-(void)AddMarkers;

-(void)locationManager:(CLLocationManager*)manager didUpdateLocations:(NSArray*)locations;
-(BOOL)mapView:(GMSMapView*)mapView didTapMarker:(GMSMarker*)marker;
@end

@implementation MapViewController {

    GMSMapView* gmapView;
    int stationIdx;

    CLLocationManager* locMan;
    float latitude;
    float longitude;
    GMSMarker* userMarker;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    // Position camera on first station location
    float lat = [(NSString*)[(NSDictionary*)([ListViewController getStations][0]) valueForKey:STATION_LATITUDE] floatValue];
    float lng = [(NSString*)[(NSDictionary*)([ListViewController getStations][0]) valueForKey:STATION_LONGITUDE] floatValue];

    GMSCameraPosition* camera = [GMSCameraPosition cameraWithLatitude:lat longitude:lng zoom:14];
    gmapView = [GMSMapView mapWithFrame:CGRectZero camera:camera];
    gmapView.translatesAutoresizingMaskIntoConstraints = NO;
    gmapView.myLocationEnabled = YES;
    gmapView.delegate = self;

    // Add station markers
    [self AddMarkers];

    // Initialize geolocation
    if ([CLLocationManager locationServicesEnabled]) {

        locMan = [[CLLocationManager alloc] init];
        locMan.delegate = self;
        [locMan requestWhenInUseAuthorization];
        [locMan startUpdatingLocation];
    }
    else
        [self.itmGeoloc setEnabled:FALSE];

    [self.mapView addSubview:gmapView];
    [self.selStation setHidden:TRUE];

    // Add constraints to the Google Map View in order to match its size and position with its parent view 'mapView'
    [self.mapView addConstraint:[NSLayoutConstraint constraintWithItem:gmapView attribute:NSLayoutAttributeWidth
                                    relatedBy:NSLayoutRelationEqual toItem:self.mapView attribute:NSLayoutAttributeWidth
                                    multiplier:1.0 constant:0.0]];
    [self.mapView addConstraint:[NSLayoutConstraint constraintWithItem:gmapView attribute:NSLayoutAttributeHeight
                                    relatedBy:NSLayoutRelationEqual toItem:self.mapView attribute:NSLayoutAttributeHeight
                                    multiplier:1.0 constant:0.0]];
    [self.mapView addConstraint:[NSLayoutConstraint constraintWithItem:gmapView attribute:NSLayoutAttributeCenterX
                                    relatedBy:NSLayoutRelationEqual toItem:self.mapView attribute:NSLayoutAttributeCenterX
                                    multiplier:1.0 constant:0.0]];
    [self.mapView addConstraint:[NSLayoutConstraint constraintWithItem:gmapView attribute:NSLayoutAttributeCenterY
                                    relatedBy:NSLayoutRelationEqual toItem:self.mapView attribute:NSLayoutAttributeCenterY
                                    multiplier:1.0 constant:0.0]];
}
- (void)viewWillDisappear:(BOOL)animated {

    [super viewWillDisappear:animated];
    if ([CLLocationManager locationServicesEnabled])
        [locMan stopUpdatingLocation];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)AddMarkers {

    int index = 0;
    for (NSDictionary* station in [ListViewController getStations]) {

        float lat = [(NSString*)[station valueForKey:STATION_LATITUDE] floatValue];
        float lng = [(NSString*)[station valueForKey:STATION_LONGITUDE] floatValue];
        GMSMarker* marker = [[GMSMarker alloc] init];

        marker.userData = [[NSNumber alloc] initWithInt:index++];
        marker.position = CLLocationCoordinate2DMake(lat, lng);
        marker.title = [NSString stringWithFormat:@"%@ - %@", station[STATION_ID], station[STATION_STREET], nil];
        marker.map = gmapView;
    }
}

//
-(IBAction)Geolocation:(id)sender {

    if ([CLLocationManager authorizationStatus] != kCLAuthorizationStatusAuthorizedWhenInUse) {
        UIAlertView* alertMsg = [[UIAlertView alloc] initWithTitle:@"Tag The Bus!"
            message:@"Géolocalisation non autorisé. Modifier les réglages de l'application pour l'autorisé."
            delegate:self cancelButtonTitle:@"Fermer" otherButtonTitles:nil];
        [alertMsg show];
        return;
    }
    if (nil == userMarker) {
        userMarker = [[GMSMarker alloc] init];
        userMarker.icon = [GMSMarker markerImageWithColor:[UIColor greenColor]];
        userMarker.userData = [[NSNumber alloc] initWithInt:-1];
        userMarker.title = @"Votre position";
        userMarker.map = gmapView;
    }
    userMarker.position = CLLocationCoordinate2DMake(latitude, longitude);
    GMSCameraPosition* camera = [GMSCameraPosition cameraWithLatitude:latitude longitude:longitude zoom:18];
    [gmapView animateToCameraPosition:camera];
}
-(void)locationManager:(CLLocationManager*)manager didUpdateLocations:(NSArray*)locations {

    CLLocation* location = [locations lastObject];
    latitude = location.coordinate.latitude;
    longitude = location.coordinate.longitude;
}

//
-(BOOL)mapView:(GMSMapView*)mapView didTapMarker:(GMSMarker*)marker {
    if ([(NSNumber*)marker.userData intValue] >= 0) {

        [self.selStation setHidden:FALSE];
        stationIdx = [(NSNumber*)marker.userData intValue];
    }
    return FALSE;
}
-(void)mapView:(GMSMapView*)mapView didTapAtCoordinate:(CLLocationCoordinate2D)coordinate {
    [self.selStation setHidden:TRUE];
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.

    if ([[segue identifier] isEqualToString:@"DisplayAlbum"]) {

        AlbumViewController* album = [segue destinationViewController];
        album.stationIdx = stationIdx;
    }
}
@end
