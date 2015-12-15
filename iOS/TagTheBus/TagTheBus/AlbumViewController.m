//
//  AlbumViewController.m
//  TagTheBus
//
//  Created by Pascal Viguié on 05/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import "AlbumViewController.h"
#import "ListViewController.h"

#import "StationData.h"
#import "AlbumData.h"
#import "PhotosCell.h"

#import "CamViewController.h"
#import "PhotoViewController.h"


@interface AlbumViewController ()
-(void)refresh;
@end

@implementation AlbumViewController {

    BOOL initialized;
    int stationID;
    NSMutableArray* photos;
}

@synthesize stationIdx;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    stationID = [[(NSDictionary*)[ListViewController getStations][stationIdx] valueForKey:STATION_ID] intValue];
    self.title = [NSString stringWithFormat:@"%d - %@", stationID,
                  [(NSDictionary*)[ListViewController getStations][stationIdx] valueForKey:STATION_STREET],
                  nil];

    [self.lstPhoto registerNib:[UINib nibWithNibName:@"PhotosView" bundle:nil]
          forCellReuseIdentifier:@"PhotoList"];

    initialized = FALSE;
    [self refresh];
}
- (void)viewDidAppear:(BOOL)animated {

    [super viewDidAppear:animated];
    if (initialized)
        [self refresh];

    initialized = TRUE;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)refresh {

    photos = [[ListViewController getDB] getAlbum:stationID];
    if (nil == photos)
        [self.lstPhoto setHidden:TRUE];

    else {

        [self.lstPhoto reloadData];
        [self.lstPhoto setHidden:FALSE];
    }
}

//
- (NSInteger)numberOfSectionsInTableView:(UITableView*)tableView {
    return 1;
}
- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section {

    if (nil == photos)
        return 0;
    return [photos count];
}
- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath {

    static NSString* simpleTableIdentifier = @"PhotoList";
    PhotosCell* cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    if (cell == nil)
        cell = [[PhotosCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:simpleTableIdentifier];

    NSDictionary* photo = [photos objectAtIndex:indexPath.row];
    cell.labPhoto.text = photo[PHOTO_TITLE];
    cell.datePhoto.text = [NSString stringWithFormat:@"Date: %@", (NSDate*)photo[PHOTO_DATE], nil];
    cell.imgPhoto.image = [UIImage imageWithContentsOfFile:[CamViewController getPicFile:(NSDate*)photo[PHOTO_DATE]
                                                                                 andType:TRUE]];
    return cell;
}
-(void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath {

    // Set landscape orientation
    NSNumber *value = [NSNumber numberWithInt:UIInterfaceOrientationLandscapeRight];
    [[UIDevice currentDevice] setValue:value forKey:@"orientation"];

    PhotoViewController* photoView = [self.storyboard instantiateViewControllerWithIdentifier:@"DisplayPhoto"];
    photoView.stationID = stationID;
    photoView.photoIdx = (int)indexPath.row;
    [self.navigationController pushViewController:photoView animated:TRUE];
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.

    if ([[segue identifier] isEqualToString:@"AddPhoto"]) {

        // Set portrait orientation
        NSNumber *value = [NSNumber numberWithInt:UIInterfaceOrientationPortrait];
        [[UIDevice currentDevice] setValue:value forKey:@"orientation"];

        CamViewController* cam = [segue destinationViewController];
        cam.stationIdx = stationIdx;
    }
}

@end
