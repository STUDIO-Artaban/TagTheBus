//
//  ViewController.m
//  TagTheBus
//
//  Created by Pascal Viguié on 25/11/2015.
//  Copyright (c) 2015 STUDIO Artaban. All rights reserved.
//

#import "ListViewController.h"
#import <SystemConfiguration/SystemConfiguration.h>

#import "StationData.h"
#import "StationsCell.h"
#import "AlbumViewController.h"


static NSString* ERR_MESSAGE_DOWNLOAD = @"Echec durant le téléchargement!";
static NSString* ERR_MESSAGE_PARSING = @"Format de données invalide!";

//
static NSArray* stations;
static Database* database;

@interface ListViewController ()
-(void)DownloadStationsList;
-(void)FillStationsList:(NSData*)list;

-(void)DisplayErr:(NSString*)msg;
@end

@implementation ListViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.itmMap setEnabled:FALSE];

    [self.lstStation setHidden:TRUE];
    [self.lstStation registerNib:[UINib nibWithNibName:@"StationsView" bundle:nil]
          forCellReuseIdentifier:@"StationList"];

    stations = [[NSArray alloc] init];
    database = [[Database alloc] init];

    // Check Internet connection
    SCNetworkReachabilityRef reacha = SCNetworkReachabilityCreateWithName(kCFAllocatorDefault, "www.google.com");
    SCNetworkReachabilityFlags flags;
    if ((SCNetworkReachabilityGetFlags(reacha, &flags)) && (flags & kSCNetworkReachabilityFlagsReachable)) {
        
        [self.imgWarning setHidden:TRUE];
        [self.labWarning setHidden:TRUE];
        [self.actLoading startAnimating];

        // Fill table view with Barcelona stations
        [self DownloadStationsList];
    }
    else {
        [self.labDownload setHidden:TRUE];
        [self.actLoading setHidden:TRUE];
    }
}

-(void)DownloadStationsList {

    NSURLRequest* reqURL = [NSURLRequest requestWithURL:[NSURL URLWithString:STATION_DATA_URL]];
    [NSURLConnection sendAsynchronousRequest:reqURL queue:[[NSOperationQueue alloc] init]
        completionHandler:^(NSURLResponse* response, NSData* data, NSError* error) {
            if (error) {
                NSLog(@"ERROR: %@ (line:%d)", [error description], __LINE__);
                [self DisplayErr:ERR_MESSAGE_DOWNLOAD];
            }
            else if (![data length]) {
                NSLog(@"ERROR: Empty data (line:%d)", __LINE__);
                [self DisplayErr:ERR_MESSAGE_DOWNLOAD];
            }
            else
                [self FillStationsList:data];
    }];
}
-(void)FillStationsList:(NSData*)list {

    NSError* err = nil;
    NSDictionary* json = [NSJSONSerialization JSONObjectWithData:list options:kNilOptions error:&err];
    if (err != nil) {

        NSLog(@"ERROR: Failed to parse JSON (line:%d)", __LINE__);
        [self DisplayErr:ERR_MESSAGE_PARSING];
    }
    else {

        NSArray* stationList = [[json objectForKey:(NSString*)JSON_DATA] objectForKey:(NSString*)JSON_STATIONS];
        stations = [stationList sortedArrayUsingComparator:^NSComparisonResult(id a, id b) {
            int first = [(NSString*)[(NSDictionary*)a valueForKey:STATION_ID] intValue];
            int second = [(NSString*)[(NSDictionary*)b valueForKey:STATION_ID] intValue];
            return (second > first)? -1:1;
        }];

        [self.itmMap setEnabled:TRUE];
        [self.labDownload setHidden:TRUE];
        [self.actLoading setHidden:TRUE];
        [self.lstStation setHidden:FALSE];

        [self.lstStation reloadData];
        [database read];
    }
}

+(NSArray*)getStations { return stations; }
+(Database*)getDB { return database; }

//
- (NSInteger)numberOfSectionsInTableView:(UITableView*)tableView {
    return 1;
}
- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section {

    if (nil == stations)
        return 0;
    return [stations count];
}
- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath {

    static NSString* simpleTableIdentifier = @"StationList";
    StationsCell* cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    if (cell == nil)
        cell = [[StationsCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:simpleTableIdentifier];

    NSDictionary* station = [stations objectAtIndex:indexPath.row];
    cell.labName.text = [NSString stringWithFormat:@"%@ - %@", station[STATION_ID], station[STATION_STREET], nil];
    cell.labBuses.text = [NSString stringWithFormat:@"BUSES: %@", station[STATION_BUSES], nil];
    return cell;
}
-(void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath {

    AlbumViewController* album = [self.storyboard instantiateViewControllerWithIdentifier:@"AlbumPhotos"];
    album.stationIdx = (int)indexPath.row;
    [self.navigationController pushViewController:album animated:TRUE];
}

//
-(void)DisplayErr:(NSString*)msg {

    [self.labDownload setText:msg];
    [self.imgWarning setHidden:FALSE];

    [self.actLoading stopAnimating];
    [self.actLoading setHidden:TRUE];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

@end
