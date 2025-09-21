/*          Program 1-3         */
/* The Method of False Position */
/*    - Linear Interpolation -  */

#define _CRT_SECURE_NO_WARNINGS

#include<stdio.h>
#include<math.h>
#include<stdlib.h>
#define f(x) (a[10]*pow((x),10)+a[9]*pow((x),9)+a[8]*pow((x),8)+a[7]*pow((x),7)+a[6]*pow((x),6)+a[5]*pow((x),5)+a[4]*pow((x),4)+a[3]*pow((x),3)+a[2]*pow((x),2)+a[1]*(x)+a[0])


void main(void)
{
	double a[11];
	double x1, x2, x3, y1, y2, y3, eps=1.0e-4;
	int it_max, it=0;
	printf("Input a3, a2, a1, a0 : ");
	scanf("%lf%lf%lf%lf%lf%lf%lf%lf%lf%lf%lf", &a[10], &a[9], &a[8], &a[7], &a[6], &a[5], &a[4], &a[3], &a[2], &a[1], &a[0]);
	do{
		printf("Lower, Upper x Guess : ");
		scanf("%lf%lf", &x1, &x2);
	}while( f(x1) * f(x2) >= 0 );//근이있는구간 채택
	printf("Input Max Iteration : ");
	scanf("%d", &it_max);
	while(1){
		it++;
		y1=f(x1);
		y2=f(x2);
		x3=(x1*y2-x2*y1)/(y2-y1);
		y3=f(x3);
		if( it>it_max ){
			printf("Iteration= %d    Check Error!!!!",it);
			exit(1);
		}
		if (y1 * y3 > 0)
			if (fabs(x3 - x1) < eps) break;
			else x1=x3;
		if( y1*y3<0 )
			if( fabs(x3-x2)<eps ) break;
			else x2=x3;
		if( y1*y3==0. ) break;
	};
	printf("nf(x) =%lfx^10 +%lfx^9 + %lfx^8 +%lfx^7 + %lfx^6 +%lfx^5 + %lfx^4 +%lfx^3 + %lfx^2 + %lfx + %lf\n", a[10], a[9], a[8], a[7], a[6], a[5], a[4], a[3], a[2], a[1], a[0]);
	printf("Root=        %lf\nIteration=  %d \n",x3,it);
}
