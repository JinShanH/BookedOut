import sys
import json
import numpy as np
import pandas as pd
import sklearn as sk
import matplotlib.pyplot as plt
import random
from sklearn.preprocessing import MinMaxScaler
from sklearn.cluster import KMeans
from sklearn import neighbors

# Number of recommendations
NUM_REC = 5

# import books dataset to dataframe
df = pd.read_csv("final.csv")

# Classify rating scores
df2 = df.copy()
df2.loc[ (df2['rating'] >= 0) & (df2['rating'] <= 1), 'rating_between'] = "0 to 1"
df2.loc[ (df2['rating'] > 1) & (df2['rating'] <= 2), 'rating_between'] = "1 to 2"
df2.loc[ (df2['rating'] > 2) & (df2['rating'] <= 3), 'rating_between'] = "2 to 3"
df2.loc[ (df2['rating'] > 3) & (df2['rating'] <= 4), 'rating_between'] = "3 to 4"
df2.loc[ (df2['rating'] > 4) & (df2['rating'] <= 5), 'rating_between'] = "4 to 5"
rating_df = pd.get_dummies(df2['rating_between'])

# Declare most popular genre types
g_types = ['fiction', 'nonfiction', 'horror', 'thriller', 'fantasy', 'mystery', 'history', 'romance', 'classic', 'crime', 'comedy']
genres = df2[g_types]

# Combine selected data to obtain dataset of features with all nominal values, so that we can apply learning models on it
features = pd.concat([rating_df, df2['rating'], df2['rating_count'], df2['pages'], df2[g_types]], axis=1)

# Normalise the data
min_max_scaler = MinMaxScaler()
features = min_max_scaler.fit_transform(features)

# Train the kNN model with the normalised data
model = neighbors.NearestNeighbors(n_neighbors=10, algorithm='auto')
model.fit(features)

# Query for indices of and distances to the neighbors of every book. 
# Each entry represents the list of nearest neighbours and their distances for each book,
#     sharing the same index of the queried book as in the dataframe. 
dist, idlist = model.kneighbors(features)



'''
    Recommend other books by the given book's author
    Returns: list of ISBN
'''
def recommend_by_author(_df, title):
    entry = _df[_df['title'] == title]
    author = entry['author'].to_numpy()
    books = _df[(_df['author'].to_numpy() == author) &
                (_df['rating'] >= 3) &
                (_df['title'] != title)]
    out = list()
    for isbn in books['isbn']:
        out.append(isbn)
    return (out)



'''
    k-Nearest Neighbours Algorithm Recommender
    Given the nearest neighbours for every book in the entire dataset, 
        get the nearest neighbours of the specified book
    Returns: list of ISBN
'''
def kNN_recommender(_df, knn_idlist, title):
    rec_titles = []
    # get index of read book in dataframe
    idx = df2[df2['title'] == title].index
    bookID = idx[0]
    # For all entries in the nearest neighbours matrix, 
    # add nearest neighbours for the book as specified by index. 
    for newid in idlist[bookID]:
        if (df2.loc[newid].title == title):
            continue
        else:
            rec_titles.append(df2.loc[newid].title) 
    # Only return books whose average rating >= 3
    books = _df[(_df['rating'] >= 3) &
                (_df['title'].isin(rec_titles))]
    out = list()
    for isbn in books['isbn']:
        out.append(isbn)
    return (out)



'''
    Get a random set of books
    Returns: list of ISBN
'''
def getRandom(_df):
    randoms = _df.sample(n = NUM_REC)
    out = list()
    for isbn in randoms['isbn']:
        out.append(isbn)
    return out



'''
    Get recommendations based on list of read books.

    If user has no read books, simply pick random recommendations
    Otherwise, for each read book, 
        generate a set of newbooks for:
            - books written by the author
            - most similar books as identified by the kNN model
    
    From the set of unique books, randomly pick out several books to recommend to the user
    Returns: list of ISBN
'''
def getRecommendations(_df, knn_idlist, readBooks):
    books = set()
    if (len(readBooks) == 0):
        # if user nas no books in main collection, recommend books at random
        for rec in getRandom(_df):
            books.add(rec)
    else:
        # otherwise, for each book generate a list of recommendations
        for title in readBooks:
            print(title)
            # generate recommendations by author
            for rec in recommend_by_author(_df, title):
                books.add(rec)
            # generate recommendations by features
            for rec in kNN_recommender(_df, knn_idlist, title):
                books.add(rec)

    # if not enough books to recommend, generate some extras
    if len(books) < NUM_REC:
        extras = getRandom(_df)
        for rec in extras:
            books.add(rec)
            if len(books) == NUM_REC:
                break
    result = random.choices(tuple(books), k=NUM_REC)
    return result



# get list of user's read books user input
input = sys.argv[1]

# read = json.loads(input)
# recommendedBooks = getRecommendations(df, idlist, read)

recommendedBooks = getRecommendations(df, idlist, input)

print(recommendedBooks)
