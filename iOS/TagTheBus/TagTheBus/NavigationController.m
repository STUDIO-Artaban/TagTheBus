//
//  NavigationController.m
//  TagTheBus
//
//  Created by Pascal Viguié on 08/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import "NavigationController.h"


@implementation NavigationController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

//
- (BOOL)shouldAutorotate {

    if([self.topViewController respondsToSelector:@selector(shouldAutorotate)])
        return [self.topViewController shouldAutorotate];

    return TRUE;
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
