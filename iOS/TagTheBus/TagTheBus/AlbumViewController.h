//
//  AlbumViewController.h
//  TagTheBus
//
//  Created by Pascal Viguié on 05/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AlbumViewController : UIViewController <UITableViewDelegate, UITableViewDataSource>

@property int stationIdx;
@property (weak, nonatomic) IBOutlet UITableView *lstPhoto;

@end
