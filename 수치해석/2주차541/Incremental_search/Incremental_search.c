/*       Program 1-1              */
/* THE INCREMENTAL-SEARCH METHOD  */
/*                                */

#define  _CRT_SECURE_NO_WARNINGS

#include<stdio.h>
#include<math.h>
#include<stdlib.h>
#define f(x) a[3]*pow((x),3)+a[2]*pow((x),2)+a[1]*(x)+a[0]

main()
{
	double a[4];
	double xi, xd, y1, y2, eps=1.0e-4, del_x=0.1;//ÀÔ½Ç·Ð ¹¹¿©, µ¨Å¸x´Â 0.1
	int it_max, it=0;
	printf("Input a[3 - 0]:");
	scanf("%lf%lf%lf%lf", &a[3], &a[2], &a[1], &a[0]);
	printf("Input [Max Iteration] & [initial x]:");
	scanf("%d%lf", &it_max, &xi);
	printf("\n Initial  dx = %f   Tolerance = %f\n", del_x, eps);
	while(1){
		it++;
		xd=xi+del_x;
		y1=f(xi);
		y2=f(xd);		
		if( y1*y2>0 )xi=xd;
		if( y1*y2<0 )
			if( del_x<eps )break;
			else del_x=del_x/10.0;
		if( y1*y2==0 )break;
		if( it>it_max ){
			printf("iteration= %d    Check Error!!!!",it);
			exit(1);
		}
	}
	printf("\nf(x) = %10.3lfx^3 + %10.3lfx^2 + %10.3lfx + %10.3lf\n",
		a[3], a[2], a[1], a[0]);
	printf("First= %10.3lf \n iteration= %d \n",xd,it);
}

