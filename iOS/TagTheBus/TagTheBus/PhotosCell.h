//
//  PhotosCell.h
//  TagTheBus
//
//  Created by Pascal Viguié on 07/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PhotosCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UIImageView *imgPhoto;
@property (weak, nonatomic) IBOutlet UILabel *labPhoto;
@property (weak, nonatomic) IBOutlet UILabel *datePhoto;

@end
