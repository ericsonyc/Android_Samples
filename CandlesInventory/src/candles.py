import csv

mintotal = 10
smalltotal = 10
regulartotal = 10
dictionary = {}


def init():
    with open('final1.csv') as f:
        f_csv = csv.reader(f)
        for row in f_csv:
            dictionary[row[0]] = row[1]


def cmp(a, b):
    return (a > b) - (a < b)


def handle():
    value = ""
    name = input("Please enter the smell name of candle you like:")
    for key in dictionary.keys():
        if cmp(key, name) == 0:
            value = dictionary[key]
    return value


def show():
    information = ""
    file = open("infos.txt", "r")
    for line in file:
        information = information + line
    information = information + "\n1.min size\n2.small size\n3.regular size"
    return information


def inventory():
    while (1):
        print(show())
        answer = input("Do you want to buy?(y/n):").strip().lower()
        if cmp(answer, "y") != 0:
            continue
        answer = int(input("Choose the size you want to buy?(1,2,3)(-1 to exit):").strip())
        if answer == -1:
            break
        handleNumber(answer)
        saveFile()


def saveFile():
    with open('result.csv', 'w', newline="") as datacsv:
        csvwriter = csv.writer(datacsv, dialect='excel')
        csvwriter.writerow(["Remaining min size:", mintotal])
        csvwriter.writerow(["Remaining small size:", smalltotal])
        csvwriter.writerow(["Remaining regular size:", regulartotal])


def handleNumber(number):
    global mintotal
    global smalltotal
    global regulartotal
    if number == 1:
        mintotal = mintotal - 1
    elif number == 2:
        smalltotal = smalltotal - 1
    elif number == 3:
        regulartotal = regulartotal - 1


if __name__ == '__main__':
    init()
    value = handle()
    if cmp(value, "") != 0:
        print("The name in English is:")
        print(value)
        print("Below is all the information about candles of different types of size:")
        inventory()
    else:
        print("Sorry! We have no same smell name as you type.")
