# Roller Coaster Throughput

**There is no starter code for this, but you need to provide the example.txt and input.txt file**

You have been hired by a large entertainment conglomerate that also runs theme parks as a side business. In order to ensure that maximum fun is
had per ride, we really want to avoid empty seats, but we also want to get maximum riders through in the minimum amount of time.

Groups of people like to be on the same roller coaster train, so we won't split groups between trains. Groups also shouldn't be split between rows if they don't need to be. Groups must be seated together in contiguous rows with no gaps between group members. For example, in a train that is 2 seats wide, a group of 4 will always be seated as `2,2` (filling 2 complete rows) and never as `1,2,1` (which would create a gap). Groups should be placed on the first available train where they can fit completely, even if this results in empty seats.

**Example 1:** For a roller coaster with 5 rows of 2 seats each (10 total seats) and 2 groups of 5 people each:

- Group 1 occupies the first 2 rows (4 seats) plus 1 seat in row 3
- Group 2 occupies 1 seat in row 3 plus the last 2 rows (4 seats)

In the diagram below, each number indicates which group that person belongs to:

```text
1 1 1 2 2
1 1 2 2 2
```

**Example 2:** Same train configuration (5 rows of 2 seats) with groups of 3, 3, and 4 people:

```text
1 1 2 3 3
1 2 2 3 3
```

Note: If these same groups arrived in a different order (3, 4, 3 instead of 3, 3, 4), we would get the same seating arrangement. This is because our algorithm considers multiple groups ahead in the queue and selects the first group that fits, maintaining the rule that groups are placed on the first available train where they can fit completely.

**Example 3:** Multi-train scenario with groups of 5, 6, 4, and 3 people:

Since the 2nd group (6 people) won't fit on the first train, but the 3rd group (4 people) will, we apply the lookahead rule and seat the group of 4 on the first available train. This results in using two trains:

(Note: `0` indicates an empty seat)

```text
1 1 1 3 3
1 1 0 3 3

2 2 2 4 4
2 2 2 4 0
```

**Example 4** In this example, we have a roller coaster with larger rows (rows of 4).

In that scanario, if we have groups if size 6, 6, 5, 3, 2, 1, and 4 arrive.

```text
1 1 2 3 3 5 7
1 1 2 3 4 5 7
1 2 2 3 4 6 7
1 2 2 3 4 0 7
```

Both groups `1` and `2` need to be split across at least 2 rows, but their remaining 2 riders can be sat together (in row 2 in this example).
Group 3 is a group of 5, and we'll split them 4 and 1, allowing group 4 to complete the second row that group 3 occupies. In a more realistic scanrio we might break them 3/2, but we run a strict theme park that strives for optimal efficiency.

## Lookahead

When filling a train, we can look ahead up to 4 groups past the current position in line (i.e., we can consider the first 5 groups total). We use a "first fit" strategy rather than "best fit" - we take the first group in this lookahead window that can fit on the current train. This prevents people from feeling like they're being cut in line.

We calculate a ride's throughput in terms of riders per hour. This means we need to know how many trains
there are, how many rows on the train, how many seats are in each row, how long the circuit takes to complete, and how long it takes to change over the riders on a given train.

All times will be given in seconds.

## Input

The input file contains multiple ride configurations. Each ride record has the following format:

```text
Ride Name
#Trains #Rows #SeatsPerRow #CircuitSeconds #ChangeSeconds
group1
group2
group3
...
groupN
```

```text
The Beast
3 18 2 250 60
groups...
```

The last group for the last ride in the file will be followed by the characters EOF without a blank line before it.

Multiple rides in the same file will be separated by a blank line.

## Example

Here is a full example with the expected output.
One train, 8 rows, with 2 riders per row. So we can have `16` riders per train.

```text
Example Ride
1 8 2 60 30
6
6
5
2
1
3
4
2
6
3
2
5
EOF
```

This can be satisfied in 3 trains, by seating the groups in this way (using index +1 to represent the group number and 0 to represent an empty seat).

```text
Train 1
[[1 1] [1 1] [1 1] [2 2] [2 2] [2 2] [4 4] [5 0]]
Train 2
[[3 3] [3 3] [3 6] [6 6] [7 7] [7 7] [8 8] [11 11]]
Train 3
[[9 9] [9 9] [9 9] [10 10] [10 12] [12 12] [12 12] [0 0]]
```

And the expected output is

```text
Ride: Example Ride
Total riders: 45
Total runs: 3
Max possible throughput: 640 riders/hour
Actual throughput: 600 riders/hour
```

## Output from the input

```shell
Ride: The Beast
Total riders: 15050
Total runs: 147
Max possible throughput: 1188 riders/hour
Actual throughput: 1129 riders/hour

Ride: Vortex
Total riders: 16650
Total runs: 209
Max possible throughput: 1512 riders/hour
Actual throughput: 1436 riders/hour
```

## Part 2

The simulation works, but we need to make some adjustments. Management is concerned about seat utilization and wants to understand how the lookahead parameter affects efficiency.

Your task is to run the simulation with different lookahead values (from 0 to 10) and analyze:

- Total number of trains needed
- Total empty seats
- Overall seat utilization percentage

A lookahead of 0 means only considering the first group in line, while a lookahead of 10 means considering up to the first 11 groups for better packing efficiency.

For the example `Example Ride` shown above, by varying the lookahead values we can get the following efficiencies.

```
=== Part 2: Empty Seat Analysis with Variable Lookahead ===

Ride: Example Ride
Lookahead | Runs | Total Riders | Empty Seats | Utilization
----------|------|--------------|-------------|------------
     0    |    4 |           45 |         19 |     70.3%
     1    |    4 |           45 |         19 |     70.3%
     2    |    3 |           45 |          3 |     93.8%
     3    |    3 |           45 |          3 |     93.8%
     4    |    3 |           45 |          3 |     93.8%
     5    |    3 |           45 |          3 |     93.8%
     6    |    3 |           45 |          3 |     93.8%
     7    |    3 |           45 |          3 |     93.8%
     8    |    3 |           45 |          3 |     93.8%
     9    |    3 |           45 |          3 |     93.8%
    10    |    3 |           45 |          3 |     93.8%

Best lookahead: 2 (utilization: 93.8%)
```

And for the long input file, we are looking for:

```
=== Part 2: Empty Seat Analysis with Variable Lookahead ===

Ride: The Beast
Lookahead | Runs | Total Riders | Empty Seats | Utilization
----------|------|--------------|-------------|------------
     0    |  153 |        15050 |        1438 |    91.3%
     1    |  150 |        15050 |        1078 |    93.3%
     2    |  148 |        15050 |         898 |    94.4%
     3    |  147 |        15050 |         826 |    94.8%
     4    |  147 |        15050 |         790 |    95.0%
     5    |  146 |        15050 |         718 |    95.4%
     6    |  146 |        15050 |         682 |    95.7%
     7    |  146 |        15050 |         682 |    95.7%
     8    |  146 |        15050 |         682 |    95.7%
     9    |  146 |        15050 |         682 |    95.7%
    10    |  146 |        15050 |         646 |    95.9%

Best lookahead: 10 (utilization: 95.9%)

Ride: Vortex
Lookahead | Runs | Total Riders | Empty Seats | Utilization
----------|------|--------------|-------------|------------
     0    |  219 |        16650 |        1718 |    90.6%
     1    |  213 |        16650 |        1242 |    93.1%
     2    |  211 |        16650 |        1018 |    94.2%
     3    |  210 |        16650 |         934 |    94.7%
     4    |  209 |        16650 |         878 |    95.0%
     5    |  208 |        16650 |         822 |    95.3%
     6    |  208 |        16650 |         822 |    95.3%
     7    |  207 |        16650 |         738 |    95.8%
     8    |  207 |        16650 |         738 |    95.8%
     9    |  207 |        16650 |         738 |    95.8%
    10    |  207 |        16650 |         738 |    95.8%

Best lookahead: 7 (utilization: 95.8%)
```
