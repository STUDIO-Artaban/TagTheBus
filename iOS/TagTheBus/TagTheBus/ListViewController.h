//
//  ViewController.h
//  TagTheBus
//
//  Created by Pascal Viguié on 25/11/2015.
//  Copyright (c) 2015 STUDIO Artaban. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Database.h"


@interface ListViewController : UIViewController <UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UIImageView *imgWarning;
@property (weak, nonatomic) IBOutlet UILabel *labWarning;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *actLoading;
@property (weak, nonatomic) IBOutlet UILabel *labDownload;
@property (weak, nonatomic) IBOutlet UITableView *lstStation;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *itmMap;

+(NSArray*)getStations;
+(Database*)getDB;

@end

