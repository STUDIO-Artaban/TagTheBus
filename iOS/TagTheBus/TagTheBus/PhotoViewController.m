//
//  PhotoViewController.m
//  TagTheBus
//
//  Created by Pascal Viguié on 10/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import "PhotoViewController.h"

#import "AlbumData.h"
#import "ListViewController.h"
#import "CamViewController.h"


@interface PhotoViewController ()

@property (weak, nonatomic) IBOutlet UIImageView *imgPhoto;
@property (weak, nonatomic) IBOutlet UIToolbar *shareToolbar;
-(IBAction)OnShare:(id)sender;

-(IBAction)OnFacebook:(id)sender;
-(IBAction)OnEmail:(id)sender;
-(IBAction)OnGooglePlus:(id)sender;
@end

@implementation PhotoViewController

@synthesize stationID;
@synthesize photoIdx;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    [self.shareToolbar setHidden:TRUE];
    
    NSMutableArray* album = [[ListViewController getDB] getAlbum:stationID];
    NSDictionary* photo = [album objectAtIndex:photoIdx];
    
    self.title = (NSString*)photo[PHOTO_TITLE];
    NSDate* datePhoto = (NSDate*)photo[PHOTO_DATE];
    self.imgPhoto.image = [UIImage imageWithContentsOfFile:[CamViewController getPicFile:datePhoto andType:FALSE]];
    
    // Resize photo according screen
    short width = [[UIScreen mainScreen] bounds].size.width;
    short height = (width * CAM_HEIGHT) / CAM_WIDTH;
    if (height > [[UIScreen mainScreen] bounds].size.height) {
    
        height = [[UIScreen mainScreen] bounds].size.height;
        width = (height * CAM_WIDTH) / CAM_HEIGHT;
    }
    [self.view addConstraint:[NSLayoutConstraint constraintWithItem:self.imgPhoto attribute:NSLayoutAttributeWidth
                        relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute
                        multiplier:1 constant:width]];
    [self.view addConstraint:[NSLayoutConstraint constraintWithItem:self.imgPhoto attribute:NSLayoutAttributeHeight
                        relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute
                        multiplier:1 constant:height]];
    
    // Add share option into navigation bar
    UIBarButtonItem* share = [[UIBarButtonItem alloc] initWithTitle:@"Partager" style:UIBarButtonItemStylePlain
                                   target:self action:@selector(OnShare:)];
    self.navigationItem.rightBarButtonItem = share;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

//
- (void) mailComposeController:(MFMailComposeViewController*)controller
           didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error {

    if (result == MFMailComposeResultFailed)
        NSLog(@"ERROR: Failed to send Email - %@", [error localizedDescription]);

    [self dismissViewControllerAnimated:YES completion:NULL];
    [self.shareToolbar setHidden:TRUE];
}

//
- (BOOL)shouldAutorotate { return FALSE; }
-(IBAction)OnShare:(id)sender {

    if ([self.shareToolbar isHidden])
        [self.shareToolbar setHidden:FALSE];
    else
        [self.shareToolbar setHidden:TRUE];
}

-(IBAction)OnFacebook:(id)sender {
    
    UIAlertView* alertMsg = [[UIAlertView alloc] initWithTitle:@"Tag The Bus!"
                        message:@"Le partage sur le réseau sociale Facebook n'est pas encore implémenté!"
                        delegate:self cancelButtonTitle:@"Ok" otherButtonTitles:nil];
    [alertMsg show];
}
-(IBAction)OnEmail:(id)sender {

    MFMailComposeViewController* mailComp = [[MFMailComposeViewController alloc] init];
    if (![MFMailComposeViewController canSendMail]) {

        UIAlertView* alertMsg = [[UIAlertView alloc] initWithTitle:@"Tag The Bus!"
                                    message:@"Envoie d'email impossible! Compte email non défini."
                                    delegate:self cancelButtonTitle:@"Ok" otherButtonTitles:nil];
        [alertMsg show];
        return;
    }
    NSMutableArray* album = [[ListViewController getDB] getAlbum:stationID];
    NSDictionary* photo = [album objectAtIndex:photoIdx];
    NSDate* datePhoto = (NSDate*)photo[PHOTO_DATE];

    mailComp.mailComposeDelegate = self;
    [mailComp setSubject:@"Tag The Bus!"];
    [mailComp setMessageBody:[NSString stringWithFormat:@"Titre de la photo ci-jointe:\n%@\nDate: %@",
                                        (NSString*)photo[PHOTO_TITLE], datePhoto, nil] isHTML:NO];
    [mailComp setToRecipients:[NSArray arrayWithObject:@"contact@studio-artaban.com"]];

    // Add photo at the email attachment
    NSData* photoData = [[NSData alloc] initWithContentsOfFile:[CamViewController getPicFile:datePhoto andType:FALSE]];
    [mailComp addAttachmentData:photoData mimeType:@"image/png" fileName:@"photo.png"];

    [self presentViewController:mailComp animated:YES completion:NULL];
}
-(IBAction)OnGooglePlus:(id)sender {

    UIAlertView* alertMsg = [[UIAlertView alloc] initWithTitle:@"Tag The Bus!"
                        message:@"Le partage sur le réseau sociale Google+ n'est pas encore implémenté!"
                        delegate:self cancelButtonTitle:@"Ok" otherButtonTitles:nil];
    [alertMsg show];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
