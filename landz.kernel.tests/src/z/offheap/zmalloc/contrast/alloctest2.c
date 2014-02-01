#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <sys/time.h>
 
const int COUNT = 1000000;
const int SIZE_OF_ALLOCATION = 1000;

static int64_t timeval_diffmsec(const struct timeval *tv2,
                                const struct timeval *tv1)
{
    int64_t delta = tv2->tv_sec - tv1->tv_sec;
 
    return delta * 1000 + (tv2->tv_usec - tv1->tv_usec) / 1000;
}
 
int main(int argc, char *argv[])
{
    char* p[COUNT];

    for (int k = 0; k < 3; k++) {
        struct timeval start;
        struct timeval end;
        struct list *head = NULL;
        struct list *tail = NULL;
 
        /* Allocation */
        gettimeofday(&start, NULL);
        for (int i = 0; i < COUNT; i++) {
            p[i] = (char*)malloc(sizeof(char)*SIZE_OF_ALLOCATION);
        }
        gettimeofday(&end, NULL);
 
        printf("%d allocations in %ldms (%ld/s)\n",
               COUNT,
               timeval_diffmsec(&end, &start),
               COUNT * 1000 / timeval_diffmsec(&end, &start));
 
        /* Deallocation */
        gettimeofday(&start, NULL);
        for (int i = 0; i < COUNT; i++) {
           free(p[i]);
        }        
        gettimeofday(&end, NULL);
        
        printf("%d deallocations in %ldms (%ld/s)\n",
               COUNT,
               timeval_diffmsec(&end, &start),
               COUNT * 1000 / timeval_diffmsec(&end, &start));
    }
 
    return 0;
}
