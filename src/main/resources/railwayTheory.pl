/*
    Prolog theory to verify if the given railway is connected
*/

reachable(X, Y) :-
    reachable(X, Y, []).

% Base case: a station can reach itself
reachable(X, X, _).

% Direct rail between two stations
reachable(X, Y, _) :-
    rail(X, Y).

% Search a path between two stations using indirect connections with others stations
reachable(X, Y, Visited) :-
    rail(X, Z),
    Z \= Y,
    \+ member(Z, Visited), % Avoid already visited stations to avoid loop
    reachable(Z, Y, [X|Visited]).

%iterate between every pair of stations and check if they are all reachable
connected_graph :-
    findall(S, station(S), Stations),
    forall(member(X, Stations),
           forall(member(Y, Stations),
                  reachable(X, Y))).
